package net.greg.examples.sphinx;

import java.io.*;

import java.sql.*;
import java.time.*;

import java.util.*;
import java.util.stream.*;

import javax.speech.*;
import javax.speech.synthesis.*;

import edu.cmu.sphinx.api.*;
import edu.cmu.sphinx.result.*;


/**
 * recognizer.startRecognition(new FileInputStream(new File("test.wav")));
 *
 * http://www.speech.cs.cmu.edu/tools/lmtool-new.html
 *
 * https://www.geeksforgeeks.org/converting-text-speech-java/
 * https://cmusphinx.github.io/wiki/tutorialsphinx4/#using-sphinx4-in-your-projects
 * https://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html
 */
public final class Flashcard {


  /* Programmatic ßrowser Launch | ßegin */
  private static String MAC_OPEN  = "/usr/bin/open";
  private static String APP_FLAG = "-a";
  private static String CHROME = "/Applications/Google Chrome.app";
  private static String FILE_PROTOCOL = "file:///";
  private static String APP_PATH = "/src/main/resources/models/";
  private static String HTML = ".HTML";
  /* Programmatic ßrowser Launch | End */


  /**
   * List of accepted words/phrases
   */
  private static final List<String> corpusFilterArray =
    new ArrayList();


  /**
   * float sampleRate, int sampleSize, bool signed, bool bigEndian);
   */
  private static final Microphone mic =
    new Microphone(16000, 16, true, false);


  /**
   * Speech Detector.
   */
  private static StreamSpeechRecognizer recognizer = null;


  /**
   * Wrapper for a recognition result.
   */
  private static SpeechResult speechResult = null;


  /**
   * Events Journalog
   */
  private static String JOURNAL = "JOURNAL.LOG";


  /**
   *
   */
  public static void main(String ... args) {

    Flashcard.prep();
  }


  private static void prep() {

    BufferedWriter journal = null;

    try {

      journal =
        new BufferedWriter(
          new FileWriter(JOURNAL));


      Instant instant =
        new Timestamp(
          System.currentTimeMillis()).
            toInstant();

      journal.write("\n" + instant);
      journal.flush();

      mic.startRecording();
      recognizer.startRecognition(mic.getStream());

      String hypothesis = "";

      while ((speechResult = recognizer.getResult()) != null) {

        hypothesis = speechResult.getHypothesis();

        System.err.println(
          "\ncorpusFilterArray: " + corpusFilterArray +
          "\nhypothesis" + hypothesis +
          corpusFilterArray.contains(hypothesis));

        if (corpusFilterArray.contains(hypothesis)) {

          journal.write("\n" + hypothesis);
          journal.flush();

          publish(hypothesis);
        }

        System.err.println(
          "\n\nHypothesis  " +
          hypothesis + "\n\n");
      }
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    finally {

      recognizer.stopRecognition();
      mic.stopRecording();

      if (null != journal) {
        try {
          journal.flush();
          journal.close();
        }
        catch (IOException e) { e.printStackTrace(); }
      }
    }
  }


  /**
   * Publish the page corresponding to the command, to Chrome
   */
  private static void publish(String command) {

    // discard first element
    command = command.split(" ")[1];

    String USERDIR =
      System.getProperty("user.dir");

    try {

      Runtime.getRuntime().
        exec(
          new String[] {
            MAC_OPEN, APP_FLAG, CHROME,
            (FILE_PROTOCOL + USERDIR + APP_PATH)  +
            command + HTML});
    }
    catch (IOException e) { e.printStackTrace(); }
  }



  private static Configuration config;

  static {

    final String ACOUSTIC_MODEL_PATH =
      "resource:/edu/cmu/sphinx/models/en-us/en-us";

    final String DICTIONARY_PATH =
      /// "resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict";
      "resource:/0295.dic";

    final String LANGUAGE_MODEL_PATH =
      /// "resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin";
      "resource:/0295.lm";

    config = new Configuration();

    config.setAcousticModelPath(ACOUSTIC_MODEL_PATH);
    config.setDictionaryPath(DICTIONARY_PATH);
    config.setLanguageModelPath(LANGUAGE_MODEL_PATH);

    try (

      BufferedReader reader =
        new BufferedReader(
          new InputStreamReader(
            Flashcard.class.
              getClassLoader().
                getResourceAsStream("corpus.txt")))) {

      String line;

      while (null != (line = reader.readLine())) {

        if (line.length() > 0 && ! line.startsWith("#")) {
          System.err.println("\n\n" + line + "\n\n");
          corpusFilterArray.add(line);
        }
      }

      System.err.println("\n\n" + corpusFilterArray + "\n\n");

    }
    catch (IOException e) {
      e.printStackTrace();
    }


    try {

      recognizer =
        new StreamSpeechRecognizer(config);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }
}
