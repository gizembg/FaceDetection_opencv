package com.example.gizem.detection_opencv;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
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

public class FaceDetectionActivity extends Activity implements CvCameraViewListener2 {

    private static final String    TAG                 = "OCVSample::Activity";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);  //rgb color. green.
    public static final int        JAVA_DETECTOR       = 0;

    private Mat                    mRgba;   //Mat is OpenCv class
    private Mat                    mGray;

    private File                   mCascadeFile;
    private CascadeClassifier      mJavaDetector;

    private File                   mCascadeFileEye;
    private CascadeClassifier      mJavaDetectorEye;

    /**CascadeClassifier()
     - Loads a classifier from a file.
     - Class for object detection
     */

    // private int                    mDetectorType       = JAVA_DETECTOR;
    private String[]               mDetectorName;
    private float                  mRelativeFaceSize   = 0.2f;
    private int mAbsoluteFaceSize = 0;

    //CameraBridgeViewBase, basic class implementing the interaction with Camera and OpenCV library. The main responsibility of it - is to control when camera can be enabled, process the frame, call external listener to make any adjustments to the frame and then draw the resulting frame to the screen. The clients shall implement CvCameraViewListener.
    private CameraBridgeViewBase   mOpenCvCameraView;
    double xCenter = -1;
    double yCenter = -1;

    //BaseLoaderCallback, basic implementation of LoaderCallbackInterface.
    //LoaderCallbackInterface, Interface for callback object in case of asynchronous initialization of OpenCV.
    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {        //Callback method, called after OpenCV library initialization.
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    // The InputStream class is the base class (superclass) of all input streams in the Java IO API.
                    // InputStream Subclasses include the FileInputStream, BufferedInputStream and the PushbackInputStream among others.
                    //Java InputStream's are used for reading byte based data, one byte at a tim
                    try {
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface); // load cascade file from application resources
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);
                        //An OutputStream is typically always connected to some data destination, like a file, network connection, pipe etc.
                        // Create a byte array.
                        // Then int variable named bytesRead to hold the number of bytes read for each read(byte[]) call,
                        // and immediately assigns bytesRead the value returned from the first read(byte[]) call. */
                        // The write(byte) method is used to write a single byte to the OutputStream.
                        // The write() method of an OutputStream takes an int which contains the byte value of the byte to write.
                        // Only the first byte of the int value is written. The rest is ignored. */
                        byte[] buffer = new byte[4096];
                        int bytesRead;  //to hold number of bytes read for each read(byte[]) call
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);  //passing along the buffer byte array as well as how many bytes were read into the array as parameters.
                        }
                        is.close();
                        os.close();


                        //yeni
                        // load cascade file from application resources
                        InputStream ise = getResources().openRawResource(R.raw.haarcascade_lefteye_2splits);
                        File cascadeDirEye = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFileEye = new File(cascadeDirEye, "haarcascade_lefteye_2splits.xml");
                        FileOutputStream ose = new FileOutputStream(mCascadeFileEye);

                        while ((bytesRead = ise.read(buffer)) != -1) {
                            ose.write(buffer, 0, bytesRead);
                        }
                        ise.close();
                        ose.close();


                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath()); //class for object detection
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());



                        //yeni
                        mJavaDetectorEye = new CascadeClassifier(mCascadeFileEye.getAbsolutePath());
                        if (mJavaDetectorEye.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier for eye");
                            mJavaDetectorEye = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFileEye.getAbsolutePath());

                        cascadeDir.delete();

                        //yeni
                        cascadeDirEye.delete();

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
        //   mOpenCvCameraView.setRotation(180.f);

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
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();

    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

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

        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++)
        {
            Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);

            xCenter = (facesArray[i].x + facesArray[i].width + facesArray[i].x) / 2;
            yCenter = (facesArray[i].y + facesArray[i].y + facesArray[i].height) / 2;
            Point center = new Point(xCenter, yCenter);
            Imgproc.circle(mRgba, center, 10, new Scalar(255, 0, 0, 255), 3);

        }


        //mGray için
        for (int i = 0; i < facesArray.length; i++)
        {
            Imgproc.rectangle(mGray, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);

            xCenter = (facesArray[i].x + facesArray[i].width + facesArray[i].x) / 2;
            yCenter = (facesArray[i].y + facesArray[i].y + facesArray[i].height) / 2;
            Point center = new Point(xCenter, yCenter);
            Imgproc.circle(mGray, center, 10, new Scalar(255, 0, 0, 255), 3);





            //yeni
            Rect r = facesArray[i];


            Rect eyearea_right = new Rect(r.x + r.width / 16,
                    (int) (r.y + (r.height / 4.5)),
                    (r.width - 2 * r.width / 16) / 2, (int) (r.height / 3.0));
            Rect eyearea_left = new Rect(r.x + r.width / 16
                    + (r.width - 2 * r.width / 16) / 2,
                    (int) (r.y + (r.height / 4.5)),
                    (r.width - 2 * r.width / 16) / 2, (int) (r.height / 3.0));

//****************gözdeki kırmızı çerçeveler
            Imgproc.rectangle(mRgba, eyearea_left.tl(), eyearea_left.br(),  //gözdeki yeşil çerçeve
                    new Scalar(255, 0, 0, 255), 2);
            Imgproc.rectangle(mRgba, eyearea_right.tl(), eyearea_right.br(),
                    new Scalar(255, 0, 0, 255), 2);

        }
        return mRgba;
        //return mGray;
    }

}



/* NOT

    InputStream inputstream = new FileInputStream("c:\\data\\input-text.txt");
    byte[] data      = new byte[1024];
    int    bytesRead = inputstream.read(data);

    while(bytesRead != -1) {
        doSomethingWithData(data, bytesRead);
        bytesRead = inputstream.read(data);
    }
    inputstream.close();

First this example create a byte array. Then it creates an int variable named bytesRead
to hold the number of bytes read for each read(byte[]) call,
and immediately assigns bytesRead the value returned from the first read(byte[]) call.

Inside the while loop the doSomethingWithData() method is called,
passing along the data byte array as well as how many bytes were read into the array as parameters.
At the end of the while loop data is read into the byte array again.

It should not take much imagination to figure out how to use the read(byte[], int offset, int length)
method instead of read(byte[]). You pretty much just replace the read(byte[])
calls with read(byte[], int offset, int length) calls. */