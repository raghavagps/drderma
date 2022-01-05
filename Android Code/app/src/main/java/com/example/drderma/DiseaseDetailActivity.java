package com.example.drderma;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DiseaseDetailActivity extends AppCompatActivity {

    TextView tv_severity, tv_causes, tv_cure;
    List<DiseaseInfoSample> listDiseases = new ArrayList<DiseaseInfoSample>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disease_detail);

        tv_severity = findViewById(R.id.tv_severity);
        tv_causes = findViewById(R.id.tv_causes);
        tv_cure = findViewById(R.id.tv_cure);

        Intent intent = getIntent();
        int diseaseCode = intent.getIntExtra("diseaseCode",999);

        Log.e("DiseaseDeatilActivity: ","disease code recieved: "+ diseaseCode);

        readCSV();

        DiseaseInfoSample diseaseDetected = listDiseases.get(diseaseCode+1);
        Log.e("DiseaseDeatilActivity: ","disease name: "+ diseaseDetected.getDiseaseName());
        tv_severity.setText(diseaseDetected.getSeverity());
        tv_causes.setText(diseaseDetected.getCauses());
        tv_cure.setText(diseaseDetected.getCure());


    }
    private void readCSV(){
        InputStream inputStream = getResources().openRawResource(R.raw.dis_info);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                DiseaseInfoSample sample = new DiseaseInfoSample();
                sample.setDiseaseName(tokens[0]);
                sample.setInfo(tokens[1]);
                sample.setSeverity(tokens[2]);
                sample.setCauses(tokens[3]);
                sample.setCure(tokens[4]);
                listDiseases.add(sample);
                Log.e("Sample Received: ", sample.toString());
            }
            Log.e("Detail: ", "array list size: "+listDiseases.size());
        }
        catch(Exception e){
            e.printStackTrace();
            }

    }
}