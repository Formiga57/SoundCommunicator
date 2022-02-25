package xyz.formiga57.soundcommunicator;

public class ToneGenerator {
    private byte[] GenerateTone(final float duration,final float freq,int sampleRate,int rampPercentage){
        int samples = (int)Math.ceil(duration*sampleRate);
        double[] sample = new double[samples];
        byte[] sound = new byte[2*samples]; // We multiply the samples by 2 as each sample requires 2 bytes (short => 65536 => 256^2) 
        int ramp = samples/rampPercentage; // Creating a ramp of amplitude to reduce clicks
        int idx = 0;
        int valRamp;
        // Generates a sinusoid by the formula f(t)=A*sin(2*pi*frequency/λ)
        // In case, λ will be the SampleRate, as we'll need to fit the wave inside the samples
        // We also multiply everything for 32767 (Int16 max value) as the max of sin can be 1 or -1
        for(int i = 0;i<samples;i++){
            sample[i] = Math.sin(2*Math.PI*i*freq/sampleRate);
        }
        // https://www.qsl.net/py4zbz/teoria/quantiz.htm
        // Now, we convert the values into a 16bits PCM
        for (final double val : sample) {
            if(idx < ramp){
                valRamp = (short) (32767*val*idx/ramp);
            }else if(idx < samples*2-ramp){
                valRamp = (short)(32767*val);
            }else{
                valRamp = (short) (32767*val*(samples*2-idx)/ramp);
            }
            // 2 Hex letters = 1 byte
            sound[idx++] = (byte) (valRamp & 0x00ff); // Returns first the least significant byte Ex.(-1=0xffff -> and bitwise = 0xff)
            sound[idx++] = (byte) ((valRamp & 0xff00) >>> 8); // Returns after the significant byte Ex.(-1=0xffff -> and bitwise = 0xff00)
            // But also, with >>> n, shifts 2^n bytes to right. Then 2^8=16 bytes, therefore 16 bytes = 2 letters, so we'll have 0xff again
            // Resulting in an inverted FF FF (0xffff)
        }
        return sound;
    }
}