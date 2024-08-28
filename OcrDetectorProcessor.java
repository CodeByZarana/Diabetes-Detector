/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.samples.vision.ocrreader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;

import static com.google.android.gms.samples.vision.ocrreader.OcrCaptureActivity.TextBlockObject;

/**
 * A very simple Processor which receives detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 */
public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {

    private GraphicOverlay<OcrGraphic> mGraphicOverlay;
    private Context context;


    OcrDetectorProcessor(GraphicOverlay<OcrGraphic> ocrGraphicOverlay , Context context) {
        mGraphicOverlay = ocrGraphicOverlay;
        this.context = context;
    }

    /**
     * Called by the detector to deliver detection results.
     * If your application called for it, this could be a place to check for
     * equivalent detections by tracking TextBlocks that are similar in location and content from
     * previous frames, or reduce noise by eliminating TextBlocks that have not persisted through
     * multiple detections.
     */
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        mGraphicOverlay.clear();
        SparseArray<TextBlock> items = detections.getDetectedItems();

        int done = 0;
        String data = "";
        for (int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);
            OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, item);
            mGraphicOverlay.add(graphic);
            data = data + item.getValue() + "<>";
        }

        data = data.split("Fasting")[1];
        data = "Fasting" + data;
        if(data.toLowerCase().contains("fasting")) // && data.toLowerCase().contains("random") && data.toLowerCase().contains("post prandial") && (data.toLowerCase().contains("hbalc") || data.toLowerCase().contains("hba1c")))
        {
            String readings = data.split("Fasting")[1];
            String value = readings.split("<>")[1];
            try {
                int intValue = Integer.parseInt(value);
                GenerateDiagnosis(intValue);
            } catch (Exception ex) {
            }
        }
    }

    /**
     * Frees the resources associated with this detection processor.
     */
    @Override
    public void release() {
        mGraphicOverlay.clear();
    }

    private void GenerateDiagnosis(int reading) {
        String data = "";
        data = "Blood Sugar Report\n";
        data = data + "=======================\n";
        data = data + "Fasting : " + reading + "\n";

        if (reading < 70 ) {
            data = data + "=======================\n";
            data = data + "“You seem to have LOW blood sugar\n";
            data = data + "=======================";
        } else {
            data = data + "=======================\n";
            data = data + "“You seem to have HIGH blood sugar\n";
            data = data + "=======================";
        }
        Intent dataIntent = new Intent();
        dataIntent.putExtra(TextBlockObject, data);
        ((Activity) context).setResult(CommonStatusCodes.SUCCESS, dataIntent);
        ((Activity) context).finish();
    }
}
