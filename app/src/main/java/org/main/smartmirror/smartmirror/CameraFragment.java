package org.main.smartmirror.smartmirror;

/**
 * Created by Master N on 11/6/2015.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

public class CameraFragment extends Fragment implements TextureView.SurfaceTextureListener {

    static final int REQUEST_ACCOUNT_PICKER = 1;
    static final int REQUEST_AUTHORIZATION = 2;
    static final int CAPTURE_IMAGE = 3;
    private static Drive service;
    private GoogleAccountCredential credential;
    private static Camera mCamera;
    private TextureView mTextureView;
    public static File picOutFile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.camera_fragment, container, false);

        mTextureView = (TextureView) view.findViewById(R.id.cameraView);
        mTextureView.setSurfaceTextureListener(this);

        credential = GoogleAccountCredential.usingOAuth2(getActivity(), Arrays.asList(DriveScopes.DRIVE));
        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);

        final Button capture = (Button) view.findViewById(R.id.takepicture);
        capture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                capture.setEnabled(false);
                takePhoto();
            }
        });
        return view;
    }

    protected static final int MEDIA_TYPE_IMAGE = 0;

    private void takePhoto() {
        mTextureView = null;
        Camera.PictureCallback pictureCB = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera cam) {

                File picFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                picOutFile = picFile;
                if (picFile == null) {
                    Log.e("takePhoto", "picFile is null");
                    return;
                }
                try {
                    FileOutputStream fos = new FileOutputStream(picFile);
                    Log.e("takePhoto", "creating file");
                    fos.write(data);
                    saveFileToDrive();
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.e("NOT FOUND takePhoto", "File not found: " + e.getMessage());
                    e.getStackTrace();
                } catch (IOException e) {
                    Log.e("IOERROR takePhoto", "I/O error writing file: " + e.getMessage());
                    e.getStackTrace();
                }
            }
        };
        mCamera.takePicture(null, null, pictureCB);
        mCamera.startPreview();
    }

    private File getOutputMediaFile(int type) {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/smartMirrorPics/");
        String timeStamp =
                new SimpleDateFormat("yyyMMdd_HHmmss", Locale.UK).format(new Date());
        if (type == MEDIA_TYPE_IMAGE) {
            final Button cap = (Button) getActivity().findViewById(R.id.takepicture);
            cap.setEnabled(true);
            return new File(dir.getPath() + File.separator + "IMG_"
                    + timeStamp + ".jpg");
        } else {
            return null;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        int cameraId = 0;
        Camera.CameraInfo info = new Camera.CameraInfo();

        for (cameraId = 0; cameraId < Camera.getNumberOfCameras(); cameraId++) {
            Camera.getCameraInfo(1, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
                break;
        }
        mCamera = Camera.open(cameraId);
        Matrix transform = new Matrix();
        Camera.Parameters param = mCamera.getParameters();
        param.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
        int rotation = getActivity().getWindowManager().getDefaultDisplay()
                .getRotation();

        switch (rotation) {
            case Surface.ROTATION_0:
                mCamera.setDisplayOrientation(90);
                mTextureView.setLayoutParams(new FrameLayout.LayoutParams(
                        previewSize.height, previewSize.width, Gravity.CENTER));
                transform.setScale(-1, 1, previewSize.height / 2, 0);
                break;

            case Surface.ROTATION_90:
                mCamera.setDisplayOrientation(0);
                mTextureView.setLayoutParams(new FrameLayout.LayoutParams(
                        previewSize.width, previewSize.height, Gravity.CENTER));
                transform.setScale(-1, 1, previewSize.width / 2, 0);
                break;

            case Surface.ROTATION_180:
                mCamera.setDisplayOrientation(270);
                mTextureView.setLayoutParams(new FrameLayout.LayoutParams(
                        previewSize.height, previewSize.width, Gravity.CENTER));
                transform.setScale(-1, 1, previewSize.height / 2, 0);
                break;

            case Surface.ROTATION_270:
                mCamera.setDisplayOrientation(180);
                mTextureView.setLayoutParams(new FrameLayout.LayoutParams(
                        previewSize.width, previewSize.height, Gravity.CENTER));
                transform.setScale(-1, 1, previewSize.width / 2, 0);
                break;
        }
        try {
            mCamera.setPreviewTexture(surface);
        } catch (IOException t) {
        }
        mTextureView.setTransform(null);//"null" used to be "transform". Use transform only for front-facing camera.
        mCamera.startPreview();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        //Camera takes care of this. Must keep this method for class
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (this.mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            this.mCamera = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            mCamera.setPreviewCallback(null);
            mTextureView = null;
            mTextureView.setSurfaceTextureListener(null);
            mCamera.release();
            mCamera = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCamera != null) {
            mTextureView = (TextureView) getActivity().findViewById(R.id.cameraView);
            mTextureView.setSurfaceTextureListener(this);
            mCamera.startPreview();
            Log.d("onResume", "opening camera view");
        }
        mTextureView = (TextureView) getActivity().findViewById(R.id.cameraView);
        mTextureView.setSurfaceTextureListener(this);
        Log.d("onResume2", "opening camera view");
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == getActivity().RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        credential.setSelectedAccountName(accountName);
                        service = getDriveService(credential);
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                } else {
                    startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
                }
                break;
            case CAPTURE_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    takePhoto();
                }
        }
    }

    private void saveFileToDrive() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // File's binary content
                    java.io.File fileContent = new java.io.File(picOutFile.getPath());
                    FileContent mediaContent = new FileContent("image/jpeg", fileContent);

                    // File's metadata.
                    com.google.api.services.drive.model.File body = new com.google.api.services.drive.model.File();
                    body.setTitle(fileContent.getName());
                    body.setMimeType("image/jpeg");

                    com.google.api.services.drive.model.File file = service.files().insert(body, mediaContent).execute();
                } catch (UserRecoverableAuthIOException e) {
                    startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    private Drive getDriveService(GoogleAccountCredential credential) {
        return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
                .build();
    }
}
