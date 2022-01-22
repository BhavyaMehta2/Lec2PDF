package com.bhavya.lec2pdf;

import static android.view.View.GONE;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import me.pqpo.smartcropperlib.view.CropImageView;
import wseemann.media.FFmpegMediaMetadataRetriever;


public class MainActivity extends AppCompatActivity {
    private ImageView image;
    private CropImageView ivCrop;
    private VideoView video;
    private TextView text;
    Bitmap crop;
    Bitmap bm;
    final Bitmap[] ret=new Bitmap[1];
    static int k;

    static File myFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "file.pdf");
    static PdfDocument pdfDoc;

    static {
        try {
            pdfDoc = new PdfDocument(new PdfWriter(myFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    static Document document = new Document(pdfDoc, new PageSize(910, 460));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String[] PERMISSIONS = {android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this, PERMISSIONS, 1);

        image = findViewById(R.id.image);
        ivCrop = findViewById(R.id.iv_crop);
        video =  findViewById(R.id.video);
        text = findViewById(R.id.text);
        Button button = findViewById(R.id.button);
        Uri mURI = Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "file.mp4"));

        button.setOnClickListener(v -> {
            k=0;
            video.setVideoURI(mURI);
            //video.start();
            try {
                imageExtract(mURI);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    void imageExtract(Uri mURI) throws IOException
    {
        FFmpegMediaMetadataRetriever fmmr = new FFmpegMediaMetadataRetriever();
        fmmr.setDataSource(mURI.toString());
        String time = fmmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);

        new Thread(() -> {
            long timeMicro = Long.parseLong(time)*1000;
            for(long i=0,j=0;i<=timeMicro;i=i+15000000,j++) {
                bm = fmmr.getFrameAtTime(i, FFmpegMediaMetadataRetriever.OPTION_CLOSEST);
                if(bm!=null) {
                    bm = imageCrop(bm);
                }
            }
            fmmr.release();
            document.close();
        }).start();
    }

    Bitmap imageCrop(Bitmap bmp)
    {
        Point[] in= new Point[4];
        in[0]=new Point(40,160);
        in[1]=new Point(960,160);
        in[2]=new Point(960,640);
        in[3]=new Point(40,640);
        runOnUiThread(() -> {
            ivCrop.setImageToCrop(bmp);
            ivCrop.setVisibility(GONE);
            crop = ivCrop.crop(in);
            ret[0]=crop;
            //OCR(crop);
            try {
                combineImages(crop);
                //document.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        image.setImageBitmap(ret[0]);
        return ret[0];
    }

    public void combineImages(Bitmap c) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        c.compress(Bitmap.CompressFormat.JPEG, 90 , bos);
        byte[] bitmapdata = bos.toByteArray();

        Image img = new Image(ImageDataFactory.create(bitmapdata));
        img.scaleToFit(img.getImageWidth(), img.getImageHeight());
        text.setText(String.valueOf(k));
        img.setFixedPosition(++k, 0, 0);

        pdfDoc.addNewPage(new PageSize(920, 480));
        document.add(img);
    }

/*
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
                                String resultText = visionText.getText();
                                for (Text.TextBlock block : visionText.getTextBlocks()) {
                                    String blockText = block.getText();

                                }
                            }
                        });

    }
 */
}