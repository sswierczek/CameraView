package com.otaliastudios.cameraview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

class TextureCameraPreview extends CameraPreview<TextureView, SurfaceTexture> {

    TextureCameraPreview(Context context, ViewGroup parent, SurfaceCallback callback) {
        super(context, parent, callback);
    }

    @NonNull
    @Override
    protected TextureView onCreateView(Context context, ViewGroup parent) {
        View root = LayoutInflater.from(context).inflate(R.layout.cameraview_texture_view, parent, false);
        parent.addView(root, 0);
        TextureView texture = root.findViewById(R.id.texture_view);
        texture.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {

            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                onSurfaceAvailable(width, height);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                onSurfaceSizeChanged(width, height);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                onSurfaceDestroyed();
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        });
        return texture;
    }

    @Override
    Class<SurfaceTexture> getOutputClass() {
        return SurfaceTexture.class;
    }

    @Override
    SurfaceTexture getOutput() {
        return getView().getSurfaceTexture();
    }

    @TargetApi(15)
    @Override
    void setDesiredSize(int width, int height) {
        super.setDesiredSize(width, height);
        if (getView().getSurfaceTexture() != null) {
            getView().getSurfaceTexture().setDefaultBufferSize(width, height);
        }
    }

    @Override
    boolean supportsCropping() {
        return true;
    }

    @Override
    protected void crop() {
        mCropTask.start();
        getView().post(new Runnable() {
            @Override
            public void run() {
                if (mDesiredHeight == 0 || mDesiredWidth == 0 ||
                        mSurfaceHeight == 0 || mSurfaceWidth == 0) {
                    mCropTask.end(null);
                    return;
                }
                float scaleX = 1f, scaleY = 1f;
                AspectRatio current = AspectRatio.of(mSurfaceWidth, mSurfaceHeight);
                AspectRatio target = AspectRatio.of(mDesiredWidth, mDesiredHeight);
                if (current.toFloat() >= target.toFloat()) {
                    // We are too short. Must increase height.
                    scaleY = current.toFloat() / target.toFloat();
                } else {
                    // We must increase width.
                    scaleX = target.toFloat() / current.toFloat();
                }

                getView().setScaleX(scaleX);
                getView().setScaleY(scaleY);

                mCropping = scaleX > 1.02f || scaleY > 1.02f;
                LOG.i("crop:", "applied scaleX=", scaleX);
                LOG.i("crop:", "applied scaleY=", scaleY);
                mCropTask.end(null);
            }
        });
    }
}
