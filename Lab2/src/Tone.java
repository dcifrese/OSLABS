import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;




public class Tone {

    static String noteToPlay = "NONE";
    static String timeToPlay = "NONE";


    public static void main(String[] args) throws Exception {

        FileReader inputStream = null;
        FileWriter outputStream = null;
        Object[] fileInput = new Object[13];

        

        try {
            inputStream = new FileReader("test.txt");

            int c;
            int k = 0;

            


            while ((c = inputStream.read()) != -1) {
                fileInput[k]= (char)c;
                //System.out.println(fileInput[k]);

                k++;   
                int size = fileInput.length;
            }
            
            for (int i=0; i<5;i++){
                char currentChar = (char)fileInput[i];
                switch (currentChar){
                    case 'A': if ((char)fileInput[i+1]=='4'){
                                noteToPlay = "A4";
                            }
                            if ((char)fileInput[i+1]=='5'){
                                noteToPlay = "A5";
                            }
                            //System.out.println(noteToPlay);
                            break;
                }

            }

        } finally {



            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }


        //System.out.println(noteToPlay);      

        final AudioFormat af =
            new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);
        Tone t = new Tone(af);
        t.playSong(song);
    }

    private static final List<BellNote> song = new ArrayList<BellNote>(){{
        System.out.println(noteToPlay+" IN SONG");
        if (noteToPlay.equals("A5")){
        add(new BellNote(Note.A5, NoteLength.QUARTER));
        }


        }};

    private final AudioFormat af;

    Tone(AudioFormat af) {
        this.af = af;
    }

   public static void processChar(int c){
        //Object[] fileInput = new Object[100];
        //int k = 0;
       // fileInput[k]= c;
        //System.out.println(fileInput[0]);

       // k++;
    }

    void playSong(List<BellNote> song) throws LineUnavailableException {
        try (final SourceDataLine line = AudioSystem.getSourceDataLine(af)) {
            line.open();
            line.start();

            for (BellNote bn: song) {
                playNote(line, bn);
            }
            line.drain();
        }
    }

    private void playNote(SourceDataLine line, BellNote bn) {
        final int ms = Math.min(bn.length.timeMs(), Note.MEASURE_LENGTH_SEC * 1000);
        final int length = Note.SAMPLE_RATE * ms / 1000;
        line.write(bn.note.sample(), 0, length);
        line.write(Note.REST.sample(), 0, 50);
    }
}

class BellNote {
    final Note note;
    final NoteLength length;

    BellNote(Note note, NoteLength length) {
        this.note = note;
        this.length = length;
    }
}

enum NoteLength {
    WHOLE(1.0f),
    HALF(0.5f),
    QUARTER(0.25f),
    EIGTH(0.125f);

    private final int timeMs;

    private NoteLength(float length) {
        timeMs = (int)(length * Note.MEASURE_LENGTH_SEC * 1000);
    }

    public int timeMs() {
        return timeMs;
    }
}

enum Note {
    // REST Must be the first 'Note'
    REST,
    A4,
    A4S,
    B4,
    C4,
    C4S,
    D4,
    D4S,
    E4,
    F4,
    F4S,
    G4,
    G4S,
    A5;

    public static final int SAMPLE_RATE = 48 * 1024; // ~48KHz
    public static final int MEASURE_LENGTH_SEC = 1;

    // Circumference of a circle divided by # of samples
    private static final double step_alpha = (2.0d * Math.PI) / SAMPLE_RATE;

    private final double FREQUENCY_A_HZ = 440.0d;
    private final double MAX_VOLUME = 127.0d;

    private final byte[] sinSample = new byte[MEASURE_LENGTH_SEC * SAMPLE_RATE];

    private Note() {
        int n = this.ordinal();
        if (n > 0) {
            // Calculate the frequency!
            final double halfStepUpFromA = n - 1;
            final double exp = halfStepUpFromA / 12.0d;
            final double freq = FREQUENCY_A_HZ * Math.pow(2.0d, exp);

            // Create sinusoidal data sample for the desired frequency
            final double sinStep = freq * step_alpha;
            for (int i = 0; i < sinSample.length; i++) {
                sinSample[i] = (byte)(Math.sin(i * sinStep) * MAX_VOLUME);
            }
        }
    }

    public byte[] sample() {
        return sinSample;
    }
}