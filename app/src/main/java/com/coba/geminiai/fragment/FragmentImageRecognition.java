package com.coba.geminiai.fragment;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;

import com.coba.geminiai.BuildConfig;
import com.coba.geminiai.R;
import com.coba.geminiai.utils.CustomLoadingDialog;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.P)
public class FragmentImageRecognition extends Fragment {

    private ImageView ivSearch;
    private MaterialButton btnCapture;
    private TextView tvResult;
    private CustomLoadingDialog progressDialog;

    private File imageFile;
    private Uri imageUri;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && imageFile != null) {
                    convertAndDisplayImage(imageFile.getAbsolutePath());
                }
            });

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Uri selectedImage = result.getData().getData();
                    if (selectedImage != null) {
                        try {
                            File file = new File(getContext().getCacheDir(), "temp.jpg");
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), selectedImage);
                            convertAndDisplayBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Gagal mengambil gambar dari galeri", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_image_recognition, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivSearch = view.findViewById(R.id.ivSearch);
        btnCapture = view.findViewById(R.id.btnCapture);
        tvResult = view.findViewById(R.id.tvResult);
        progressDialog = new CustomLoadingDialog(getContext());

        btnCapture.setOnClickListener(v -> showPictureDialog());
    }

    private void showPictureDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Upload Foto")
                .setItems(new CharSequence[]{"Pilih Foto", "Ambil Foto Sekarang"}, (dialog, which) -> {
                    if (which == 0) pickImageFromGallery();
                    else takePhoto();
                }).show();
    }

    private void takePhoto() {
        Dexter.withContext(requireContext())
                .withPermissions(Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(@NonNull MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            try {
                                imageFile = createImageFile();
                                imageUri = FileProvider.getUriForFile(requireContext(),
                                        BuildConfig.APPLICATION_ID + ".provider", imageFile);
                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                                cameraLauncher.launch(intent);
                            } catch (IOException e) {
                                Toast.makeText(getContext(), "Gagal membuka kamera", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(@NonNull List<PermissionRequest> permissions, @NonNull PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void pickImageFromGallery() {
        Dexter.withContext(requireContext())
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(@NonNull MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            galleryLauncher.launch(intent);
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(@NonNull List<PermissionRequest> permissions, @NonNull PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("IMG_" + timeStamp, ".jpg", storageDir);
    }

    private void convertAndDisplayImage(String path) {
        File imgFile = new File(path);
        if (!imgFile.exists()) return;

        Bitmap bitmap = decodeFile(imgFile);
        if (bitmap != null) {
            convertAndDisplayBitmap(bitmap);
        } else {
            Toast.makeText(getContext(), "Gagal mengonversi gambar.", Toast.LENGTH_SHORT).show();
        }
    }

    private void convertAndDisplayBitmap(Bitmap bitmap) {
        progressDialog.show();
        ivSearch.setImageBitmap(bitmap);
        buttonImageRecognitionGemini(bitmap);
    }

    private Bitmap decodeFile(File file) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            options.inSampleSize = calculateInSampleSize(options, 612, 816);
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Matrix matrix = new Matrix();
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) matrix.postRotate(90);
            else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) matrix.postRotate(180);
            else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) matrix.postRotate(270);

            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            int resizedHeight = (int) (rotatedBitmap.getHeight() * (512.0 / rotatedBitmap.getWidth()));
            return Bitmap.createScaledBitmap(rotatedBitmap, 512, resizedHeight, true);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private void buttonImageRecognitionGemini(Bitmap bitmap) {
        GenerativeModel generativeModel = new GenerativeModel("gemini-2.0-flash", "AIzaSyCJAGE-9VTJyuomclRJ5_ipmThu0vFxFoE");
        GenerativeModelFutures modelFutures = GenerativeModelFutures.from(generativeModel);

        Content content = new Content.Builder()
                .addText("Apa isi gambar ini?")
                .addImage(bitmap)
                .build();

        ListenableFuture<GenerateContentResponse> future = modelFutures.generateContent(content);
        Futures.addCallback(future, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(@NonNull GenerateContentResponse result) {
                tvResult.setText(result.getText());
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                tvResult.setText("Error: " + t.getMessage());
                progressDialog.dismiss();
            }
        }, requireContext().getMainExecutor());
    }
}
