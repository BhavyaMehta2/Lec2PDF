package com.bhavya.lec2pdf;

import static android.view.View.GONE;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;

import me.pqpo.smartcropperlib.view.CropImageView;
import wseemann.media.FFmpegMediaMetadataRetriever;


public class MainActivity extends AppCompatActivity {
    private ImageView image;
    private CropImageView ivCrop;
    private VideoView video;
    private TextView text;
    Bitmap crop;
    Bitmap bm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String[] PERMISSIONS = {android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this, PERMISSIONS, 1);

        image = findViewById(R.id.image);
        ivCrop = findViewById(R.id.iv_crop);
        video =  findViewById(R.id.video);
        text =  findViewById(R.id.text);
        Button button = findViewById(R.id.button);
        Uri mURI = Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "file.mp4"));

        button.setOnClickListener(v -> {
            video.setVideoURI(mURI);
            //video.start();
            imageExtract(mURI);
        });
    }

    void imageExtract(Uri mURI)
    {
        new Thread(new Runnable() {
            public void run() {
                FFmpegMediaMetadataRetriever fmmr = new FFmpegMediaMetadataRetriever();
                fmmr.setDataSource(mURI.toString());
                String time = fmmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);
                long timeMicro = Long.parseLong(time)*1000;
                for(long i=0,j=0;i<=timeMicro;i=i+5000000,j++) {
                    bm = fmmr.getFrameAtTime(i, FFmpegMediaMetadataRetriever.OPTION_CLOSEST);
                    if(bm!=null)
                        bm = imageCrop(bm);
                }
                fmmr.release();
            }
        }).start();
    }

    Bitmap imageCrop(Bitmap bmp)
    {
        Point in[]= new Point[4];
        in[0]=new Point(50,180);
        in[1]=new Point(960,180);
        in[2]=new Point(960,640);
        in[3]=new Point(50,640);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ivCrop.setImageToCrop(bmp);
                ivCrop.setVisibility(GONE);
                crop = ivCrop.crop(in);
                OCR(crop);
                image.setImageBitmap(crop);
            }
        });
        return crop;
    }

    void OCR(Bitmap crop)
    {
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        InputImage image1 = InputImage.fromBitmap(crop,0);
        Task<Text> result =
                recognizer.process(image1)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                text.setText(visionText.getText());
                            }
                        });
    }
}