package xyz.formiga57.soundcommunicator;

import java.nio.charset.StandardCharsets;

public class TextModulator {
    public void PlayText(String text){
        byte[] arrayString = text.getBytes(StandardCharsets.UTF_8);
        int[] arrayInt = new int[arrayString.length];
        for(int i=0;i<arrayString.length;i++){
            arrayInt[i] = arrayString[i]*20;
        }
        new Thread(() -> {
            ToneGenerator toneGenerator = new ToneGenerator(44100);
            for (int i: arrayInt) {
                toneGenerator.PlayTone(0.25f,i,1,false);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    
}
