package xyz.formiga57.soundcommunicator;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.core.content.pm.PermissionInfoCompat;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Permissions;
import java.security.acl.Permission;
import java.util.Arrays;

import xyz.formiga57.soundcommunicator.R;

public class MainActivity extends AppCompatActivity {
    private final String TesteString = "FAZENDO O TESTE DE STRING BEM LEGAL PARA TENTAR TRANSFERIR UMA MENSAGEM";
    
    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;

    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        byte[] arrayString = TesteString.getBytes(StandardCharsets.UTF_8);
        float[] arrayFloat = new float[arrayString.length];
        for(int i=0;i<arrayString.length;i++){
            arrayFloat[i] = arrayString[i]*20;
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED){
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},57);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_MEDIA_LOCATION) == PackageManager.PERMISSION_DENIED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_MEDIA_LOCATION},57);
        }
        AudioFormat.Builder audioFormatBuilder = new AudioFormat.Builder();
        ;
        AudioRecord.Builder builder = new AudioRecord.Builder();
        AudioRecord audioRecord = builder.setAudioSource(MediaRecorder.AudioSource.MIC)
                .setAudioFormat(audioFormatBuilder.setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(44100).build())
                .setAudioSource(MediaRecorder.AudioSource.MIC)
                .setBufferSizeInBytes(1024*2)
                .build();
        short[] audioData = new short[1024];
        
        final Thread thread = new Thread(new Runnable() {
            public void run() {
//                ToneGenerator tg = new ToneGenerator();
                ContextWrapper cw = new ContextWrapper(getApplicationContext());
                File directory = new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)));
                File file = new File(directory,"voice8K16bitmononovaversaoAgoravai.pcm");
                FileOutputStream os = null;
                try {
                    os = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                audioRecord.startRecording();
                for(int i =0;i<2000;i++) {
                    audioRecord.read(audioData,0,1024);
                    String audiodataString = Arrays.toString(audioData);
                    byte bData[] = short2byte(audioData);
                    try {
                        os.write(bData,0,1024*2);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.d("ITERATION",String.valueOf(i));
                }
                audioRecord.stop();
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //                while(true){
//                    for (final float freq:arrayFloat) {
//                        tg.PlayTone(44100,freq,0.1f);
//                        try {
//                            Thread.sleep(250);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
            }
        });
        thread.start();
    }
    public class ToneGenerator {
        private float Duration = 5;
        private int SampleRate = 8000;
        private int Samples = (int)Math.ceil(Duration*SampleRate);
        private double[] Sample = new double[Samples];
        private double Freq = 100;
        private byte[] Sound = new byte[2*Samples];
        AudioTrack.Builder playerBuilder;
        ToneGenerator(){
            playerBuilder = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(44100)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build());
        }
        private byte[] GenerateTone(){
            // Generates a sinusoid by the formula f(t)=A*sin(2*pi*frequency/λ)
            // In case, λ will be the SampleRate, as we'll need to fit the wave inside the samples
            // We also multiply everything for 32767 (Int16 max value) as the max of sin can be 1 or -1
           for(int i = 0;i<Samples;i++){
               Sample[i] = Math.sin(2*Math.PI*i*Freq/SampleRate);
           }
            // Converting to 16bit pcm array
            int idx = 0;
            int ramp = 1;
            short valRamp = 0;
            short[] valores = new short[Samples*2];
            for (final double val : Sample) {
                if(idx < ramp){
                    valRamp = (short) (32767*val*idx/ramp);
                }else if(idx < Samples*2-ramp){
                    valRamp = (short)(32767*val);
                }else{
                    valRamp = (short) (32767*val*(Samples*2-idx)/ramp);
                }
                valores[idx] = valRamp;
                Sound[idx++] = (byte) (valRamp & 0x00ff);
                valores[idx] = valRamp;
                Sound[idx++] = (byte) ((valRamp & 0xff00) >>> 8);
            }
            return Sound;
        }
        public void PlayToneCont(int sampleRate,double freq){
            this.Freq = freq;
            this.SampleRate = sampleRate;
            this.Samples = (int)Math.ceil(Duration*SampleRate);
            this.Sample = new double[Samples];
            this.Sound = new byte[2*Samples];
            byte[] soundGenerated = GenerateTone();
            AudioTrack player = playerBuilder.setBufferSizeInBytes(soundGenerated.length).build();
            while(true){
                player.write(soundGenerated,0,soundGenerated.length);
                try{
                    Thread.sleep(1000);
                }catch (InterruptedException e){
                }
            }
        }
        public void PlayTone(int sampleRate,double freq,float duration){
            this.Duration = duration;
            this.Freq = freq;
            this.SampleRate = sampleRate;
            this.Samples = (int)Math.ceil(Duration*SampleRate);
            this.Sample = new double[Samples];
            this.Sound = new byte[2*Samples];
            byte[] soundGenerated = GenerateTone();
            AudioTrack player = playerBuilder.setBufferSizeInBytes(soundGenerated.length).build();
            player.setNotificationMarkerPosition(soundGenerated.length/2);
            player.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
                @Override
                public void onMarkerReached(AudioTrack track) {
                    player.release();
                }
                @Override
                public void onPeriodicNotification(AudioTrack track) {
                }
            });
            player.play();
            player.write(soundGenerated,0,soundGenerated.length);
        }
    }
}