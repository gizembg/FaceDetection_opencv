package com.example.gizem.detection_opencv;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static org.opencv.core.CvType.CV_8UC1;

public class FaceDetectionActivity extends Activity implements CvCameraViewListener2 {

    private static final String    TAG                 = "OCVSample::Activity";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);  //rgb color. green.
    public static final int        JAVA_DETECTOR       = 0;
    private Mat                    mRgba;
    private Mat                    mGray;
    private File                   mCascadeFile;
    private CascadeClassifier      mJavaDetector;
    /**CascadeClassifier()
     - Loads a classifier from a file.
     - Class for object detection
     */

    private String[]               mDetectorName;
    private float                  mRelativeFaceSize   = 0.2f;
    private int mAbsoluteFaceSize = 0;

    private CameraBridgeViewBase   mOpenCvCameraView;
    double xCenter = -1;
    double yCenter = -1;

    ImageView imageview;
    Bitmap icon;
    Mat img_object;

    Mat src_roi;
    Mat roi_gray;
    Mat roi_rgb;
    Rect roi;
    Button btn;
    ImageButton ibtn;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    try {
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface); // load cascade file from application resources
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);
                        byte[] buffer = new byte[4096];
                        int bytesRead;  //to hold number of bytes read for each read(byte[]) call
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);  //passing along the buffer byte array as well as how many bytes were read into the array as parameters.
                        }
                        is.close();
                        os.close();


                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath()); //class for object detection
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        cascadeDir.delete();

                        //yeni
                        Resources res = getResources();
                        icon = BitmapFactory.decodeResource(res, R.drawable.two);// get data from drawable folder

                        //TypedArray imgs = getResources().obtainTypedArray(R.array.random_imgs);
                      //  icon = BitmapFactory.decodeResource(res,imgs.getResourceId(0, 2));


                   //     imageview.setImageBitmap(icon); // show


                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }
                    mOpenCvCameraView.enableFpsMeter();
                    mOpenCvCameraView.setCameraIndex(1);
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public FaceDetectionActivity() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_face_detection);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);

        //yeni
        imageview = (ImageView)findViewById(R.id.imageview);
    //    btn = (Button)findViewById(R.id.change);
      //  ibtn = (ImageButton)findViewById(R.id.ichange);

    }
    public void onClick(View v) {
        if(v.getId() == R.id.button1) {
            Resources res = getResources();
            icon = BitmapFactory.decodeResource(res, R.drawable.one);// get data from drawable folder
        }
        if(v.getId() == R.id.button2) {
            Resources res = getResources();
            icon = BitmapFactory.decodeResource(res, R.drawable.two);// get data from drawable folder
        }
        if(v.getId() == R.id.button3) {
            Resources res = getResources();
            icon = BitmapFactory.decodeResource(res, R.drawable.three);// get data from drawable folder
        }
        if(v.getId() == R.id.button4) {
            Resources res = getResources();
            icon = BitmapFactory.decodeResource(res, R.drawable.four);// get data from drawable folder
        }
        if(v.getId() == R.id.button5) {
            Resources res = getResources();
            icon = BitmapFactory.decodeResource(res, R.drawable.five);// get data from drawable folder
        }
        if(v.getId() == R.id.button6) {
            Resources res = getResources();
            icon = BitmapFactory.decodeResource(res, R.drawable.six);// get data from drawable folder
        }
        if(v.getId() == R.id.button7) {
            Resources res = getResources();
            icon = BitmapFactory.decodeResource(res, R.drawable.seven);// get data from drawable folder
        }
        if(v.getId() == R.id.button8) {
            Resources res = getResources();
            icon = BitmapFactory.decodeResource(res, R.drawable.eight);// get data from drawable folder
        }
        if(v.getId() == R.id.button9) {
            Resources res = getResources();
            icon = BitmapFactory.decodeResource(res, R.drawable.nine);// get data from drawable folder
        }
        if(v.getId() == R.id.button10) {
            Resources res = getResources();
            icon = BitmapFactory.decodeResource(res, R.drawable.ten);// get data from drawable folder
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();

        //yeni
         img_object = new Mat(157, 140, CV_8UC1);
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();

        //yeni
        img_object.release();
    }



    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();


        //yeni
        Utils.bitmapToMat(icon, img_object);//convert image bitmap to Mat to use it futher
        Log.i("aaa", String.valueOf(img_object.cols()));
        Log.i("aaa1", String.valueOf(img_object.rows()));


        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }

        MatOfRect faces = new MatOfRect();

        if (mJavaDetector != null)
            mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());

        else {
            Log.e(TAG, "Detector is null!");
        }

        //mRgba

        img_object.copyTo(mRgba.rowRange(1, 6).colRange(3, 10));

        //Bitmap bMap= BitmapFactory.decodeResource(getResources(),R.drawable.monkey_head);
        //Mat img = Imgcodecs.imread("R.drawable.monkey_head", Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);

        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++)
        {     //   putMask(img_object, p, facesArray.length());

            Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);

            xCenter = (facesArray[i].x + facesArray[i].width + facesArray[i].x) / 2;
            yCenter = (facesArray[i].y + facesArray[i].y + facesArray[i].height) / 2;
            Point center = new Point(xCenter, yCenter);
            Imgproc.circle(mRgba, center, 10, new Scalar(255, 0, 0, 255), 3);
            Log.d("FaceDetectionActivity", "facesArray:" +facesArray[i]);


            mRgba=putMask(mRgba,center, new Size(facesArray[i].width, facesArray[i].height));




        }
        return mRgba;
        //  return mGray;
    }

    Mat putMask(Mat src, Point center, Size face_size)
    {
        Mat mask_resized = new Mat();
        src_roi = new Mat(); //ROI
        roi_gray = new Mat();
        Imgproc.resize(img_object ,mask_resized,face_size);

        // ROI selection
        roi = new Rect((int) (center.x - face_size.width/2), (int) (center.y - face_size.height/2),(int) face_size.width, (int) face_size.height);
        //Rect roi = new Rect(10, 10, (int) face_size.width, (int) face_size.height);

        src.submat(roi).copyTo(src_roi);

        Log.e(TAG, "MASK SRC1 :"+ src_roi.size());

        // to make the white region transparent
        Mat mask_grey = new Mat(); //greymask
        roi_rgb = new Mat();
        Imgproc.cvtColor(mask_resized,mask_grey, Imgproc.COLOR_BGRA2GRAY);
        Imgproc.threshold(mask_grey,mask_grey,230,255, Imgproc.THRESH_BINARY_INV);

        ArrayList<Mat> maskChannels = new ArrayList<>(4);
        ArrayList<Mat> result_mask = new ArrayList<>(4);
        result_mask.add(new Mat());
        result_mask.add(new Mat());
        result_mask.add(new Mat());
        result_mask.add(new Mat());

        Core.split(mask_resized, maskChannels);

        Core.bitwise_and(maskChannels.get(0),mask_grey, result_mask.get(0));
        Core.bitwise_and(maskChannels.get(1),mask_grey, result_mask.get(1));
        Core.bitwise_and(maskChannels.get(2),mask_grey, result_mask.get(2));
        Core.bitwise_and(maskChannels.get(3),mask_grey, result_mask.get(3));

        Core.merge(result_mask, roi_gray);

        Core.bitwise_not(mask_grey,mask_grey);

        ArrayList<Mat> srcChannels = new ArrayList<>(4);
        Core.split(src_roi, srcChannels);
        Core.bitwise_and(srcChannels.get(0),mask_grey, result_mask.get(0));
        Core.bitwise_and(srcChannels.get(1),mask_grey, result_mask.get(1));
        Core.bitwise_and(srcChannels.get(2),mask_grey, result_mask.get(2));
        Core.bitwise_and(srcChannels.get(3),mask_grey, result_mask.get(3));

        Core.merge(result_mask, roi_rgb);

        Core.addWeighted(roi_gray,1, roi_rgb,1,0, roi_rgb);

        roi_rgb.copyTo(new Mat(src,roi));

        return src;
    }




}


