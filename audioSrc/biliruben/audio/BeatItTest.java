package biliruben.audio;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import beatit.BPM2SampleProcessor;
import beatit.EnergyOutputAudioDevice;

public class BeatItTest {

    /**
     * @param args
     * @throws JavaLayerException 
     * @throws FileNotFoundException 
     */
    public static void main(String[] args) throws FileNotFoundException, JavaLayerException {
        // TODO Auto-generated method stub
        for (String arg : args) {
            BPM2SampleProcessor processor = new BPM2SampleProcessor();
            processor.setSampleSize(1024);
            EnergyOutputAudioDevice output = new EnergyOutputAudioDevice(processor);
            output.setAverageLength(1024);
            Player player = new Player(new FileInputStream(arg), output);
            player.play();
            System.out.println(arg + " - calculated BPM: " + processor.getBPM());
        }
    }

}
