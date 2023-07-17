package com.eyecool.face.duallive.demo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.eyecool.face.duallive.demo.config.DualFaceConfig;
import com.eyecool.face.duallive.demo.fragment.DualSysCameraFragment;
import com.eyecool.utils.DensityUtils;

import java.util.List;

/**
 * 双模人脸检活
 * <p>
 * Created by wangzhi on 2017/8/2.
 */
public final class DualSysCameraActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = DualSysCameraActivity.class.getSimpleName();

    private DualSysCameraFragment mDualSysCameraFragment;
    private ImageView mResultIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dual_sys_camera);

        findViewById(R.id.startCameraBtn).setOnClickListener(this);
        findViewById(R.id.stopCameraBtn).setOnClickListener(this);
        findViewById(R.id.startDetectBtn).setOnClickListener(this);
        findViewById(R.id.stopDetectBtn).setOnClickListener(this);
        findViewById(R.id.settingBtn).setOnClickListener(this);

        mResultIv = findViewById(R.id.resultIv);
        mDualSysCameraFragment = new DualSysCameraFragment();

        // 添加检活布局
        getSupportFragmentManager().beginTransaction()
                .add(R.id.dualFaceFL, mDualSysCameraFragment)
                .commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startCameraBtn:
                startCamera();
                break;
            case R.id.stopCameraBtn:
                stopCamera();
                break;
            case R.id.startDetectBtn:
                startDetect();
                break;
            case R.id.stopDetectBtn:
                stopDetect();
                break;
            case R.id.settingBtn:
                startSetting();
                break;
            default:
                break;
        }
    }

    private void startCamera() {
        mDualSysCameraFragment.startCamera();
    }

    private void stopCamera() {
        mDualSysCameraFragment.stopCamera();
    }

    private void startDetect() {
        /**
         * 参数设置一览
         *
         * DualFaceConfig.sRgbCameraId = CameraConfig.CAMERA_0; // 可见光摄像头id
         * // 可见光参数
         * DualFaceConfig.sRgbPreviewOrientation = CameraConfig.ROTATE_0; // 预览角度
         * DualFaceConfig.sRgbRotate = CameraConfig.ROTATE_0; // 数据旋转角度
         *
         * // 近红外参数
         *  DualFaceConfig.sNirPreviewOrientation = CameraConfig.ROTATE_0; // 预览角度
         * DualFaceConfig.sNirRotate = CameraConfig.ROTATE_0; // 数据旋转角度
         *
         * // 检活参数设置
         * DualFaceConfig.getDualFaceConfig().setTimeout(10); // 超时
         * DualFaceConfig.getDualFaceConfig().setThreshold(0.7f); // 检活阈值
         * DualFaceConfig.getDualFaceConfig().setDistanceMin(60); // 最小检测人脸
         * DualFaceConfig.getDualFaceConfig().setDistanceMax(250); // 最大检测人脸
         * DualFaceConfig.getDualFaceConfig().setYawDegree(10); // 摇头角度
         * DualFaceConfig.getDualFaceConfig().setPitchDegree(10); // 点头角度
         * DualFaceConfig.getDualFaceConfig().setRollDegree(10); // 歪头角度
         */
        mResultIv.setImageBitmap(null);
        mDualSysCameraFragment.startDetect(DualFaceConfig.getDualFaceConfig(), new DualSysCameraFragment.DetectCallback() {
            @Override
            public void onSuccess(List<byte[]> images) {
                byte[] nirJpg = images.get(0); // 红外原图
                byte[] rgbJpg = images.get(1); // 可见光原图
                byte[] faceCropJpg = images.get(2); // 人脸裁剪图

                Bitmap faceCropBitmap = BitmapFactory.decodeByteArray(faceCropJpg, 0, faceCropJpg.length);
                showResultDialog(faceCropBitmap);
            }

            @Override
            public void onError(int errCode, String msg) {
                Toast.makeText(DualSysCameraActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showResultDialog(Bitmap bitmap) {
        ImageView iv = new ImageView(this);
        iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        iv.setPadding(0, DensityUtils.dip2px(this, 10), 0, 0);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv.setImageBitmap(bitmap);

        DialogInterface.OnClickListener pl = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("采集结果").setView(iv)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, pl)
                .show();
    }

    private void stopDetect() {
        mDualSysCameraFragment.stopDetect();
    }

    private void startSetting() {
        startActivity(new Intent(this, SettingActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

