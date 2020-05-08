package it.unimol.understandability.ui.panels.config;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import it.unimol.understandability.core.preferences.UnderstandabilityPreferences;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;

/**
 * Created by simone on 01/02/17.
 */
public class SettingsDialog implements Configurable {
    private JPanel panel1;
    private JButton popularitiesButton;
    private JTextField stackoverflowApiKeyField;
    private JButton cacheButton;
    private JButton stopWordButton;
    private JButton expansionButton;
    private JButton readabilityClassifierButton;
    private JButton dictionaryButton;
    private JTextField snippetStartingId;
    private JTextField classStartingId;

    private String popularitiesFile;
    private String stackOverflowCacheFile;
    private String stopWordFile;
    private String expansionFile;
    private String readabilityClassifierFile;
    private String dictionaryFile;

    public SettingsDialog() {
        this.reset();

        popularitiesButton.addActionListener(actionEvent -> {
            JFileChooser chooser = getFileChooser(UnderstandabilityPreferences.getPopularityFile());

            chooser.addActionListener(actionEvent1 -> {
                File selectedFile = chooser.getSelectedFile();
                if (selectedFile != null)
                    popularitiesFile = selectedFile.toString();
                updateButtons();
            });

            chooser.showOpenDialog(panel1);
        });

        cacheButton.addActionListener(actionEvent -> {
            JFileChooser chooser = getFileChooser(UnderstandabilityPreferences.getPopularityFile());

            chooser.addActionListener(actionEvent1 -> {
                File selectedFile = chooser.getSelectedFile();
                if (selectedFile != null)
                    stackOverflowCacheFile = selectedFile.toString();
                updateButtons();
            });

            chooser.showOpenDialog(panel1);
        });

        stopWordButton.addActionListener(actionEvent -> {
            JFileChooser chooser = getFileChooser(UnderstandabilityPreferences.getStopWordFile());


            chooser.addActionListener(actionEvent1 -> {
                File selectedFile = chooser.getSelectedFile();
                if (selectedFile != null)
                    stopWordFile = selectedFile.toString();
                updateButtons();
            });

            chooser.showOpenDialog(panel1);
        });

        expansionButton.addActionListener(actionEvent -> {
            JFileChooser chooser = getFileChooser(UnderstandabilityPreferences.getExpansionFile());

            chooser.addActionListener(actionEvent1 -> {
                File selectedFile = chooser.getSelectedFile();
                if (selectedFile != null)
                    expansionFile = selectedFile.toString();
                updateButtons();
            });

            chooser.showOpenDialog(panel1);
        });

        dictionaryButton.addActionListener(actionEvent -> {
            JFileChooser chooser = getFileChooser(UnderstandabilityPreferences.getDictionaryFile());

            chooser.addActionListener(actionEvent1 -> {
                File selectedFile = chooser.getSelectedFile();
                if (selectedFile != null)
                    dictionaryFile = selectedFile.toString();
                updateButtons();
            });

            chooser.showOpenDialog(panel1);
        });

        readabilityClassifierButton.addActionListener(actionEvent -> {
            JFileChooser chooser = getFileChooser(UnderstandabilityPreferences.getReadabilityClassifierFile());

            chooser.addActionListener(actionEvent1 -> {
                File selectedFile = chooser.getSelectedFile();
                if (selectedFile != null)
                    readabilityClassifierFile = selectedFile.toString();
                updateButtons();
            });

            chooser.showOpenDialog(panel1);
        });
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Understandability";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "No help topic";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return this.panel1;
    }

    @Override
    public boolean isModified() {
        return UnderstandabilityPreferences.isChangedStackOverflowApiKey(this.stackoverflowApiKeyField.getText()) ||
                UnderstandabilityPreferences.isChangedPopularityFile(this.popularitiesFile) ||
                UnderstandabilityPreferences.isChangedStackOverflowCacheFile(this.stackOverflowCacheFile) ||
                UnderstandabilityPreferences.isChangedStopWordFile(this.stopWordFile) ||
                UnderstandabilityPreferences.isChangedExpansionFile(this.expansionFile) ||
                UnderstandabilityPreferences.isChangedReadabilityClassifierFile(this.readabilityClassifierFile) ||
                UnderstandabilityPreferences.isChangedDictionaryFile(this.dictionaryFile) ||
                UnderstandabilityPreferences.isChangedSnippetId(this.snippetStartingId.getText()) ||
                UnderstandabilityPreferences.isChangedClassId(this.classStartingId.getText());
    }

    @Override
    public void apply() throws ConfigurationException {
        UnderstandabilityPreferences.setStackOverflowApiKey(this.stackoverflowApiKeyField.getText());
        UnderstandabilityPreferences.setPopularityFile(this.popularitiesFile);
        UnderstandabilityPreferences.setStackOverflowCacheFile(this.stackOverflowCacheFile);
        UnderstandabilityPreferences.setStopWordFile(this.stopWordFile);
        UnderstandabilityPreferences.setExpansionFile(this.expansionFile);
        UnderstandabilityPreferences.setReadabilityClassifierFile(this.readabilityClassifierFile);
        UnderstandabilityPreferences.setDictionaryFile(this.dictionaryFile);

        UnderstandabilityPreferences.setSnippetId(Integer.parseInt(this.snippetStartingId.getText()));
        UnderstandabilityPreferences.setClassId(Integer.parseInt(this.classStartingId.getText()));
    }

    @Override
    public void reset() {
        this.popularitiesFile = UnderstandabilityPreferences.getPopularityFile();
        this.stackOverflowCacheFile = UnderstandabilityPreferences.getStackOverflowCacheFile();
        this.expansionFile = UnderstandabilityPreferences.getExpansionFile();
        this.stopWordFile = UnderstandabilityPreferences.getStopWordFile();
        this.readabilityClassifierFile = UnderstandabilityPreferences.getReadabilityClassifierFile();
        this.dictionaryFile = UnderstandabilityPreferences.getDictionaryFile();

        this.stackoverflowApiKeyField.setText(UnderstandabilityPreferences.getStackOverflowApiKey());
        this.snippetStartingId.setText(String.valueOf(UnderstandabilityPreferences.getSnippetId()));
        this.classStartingId.setText(String.valueOf(UnderstandabilityPreferences.getClassId()));
        updateButtons();
    }

    private JFileChooser getFileChooser(String initial) {
        JFileChooser chooser = new JFileChooser(initial);

        return chooser;
    }

    private void updateButtons() {
        this.popularitiesButton.setText("Browse (" + popularitiesFile + ")");
        this.cacheButton.setText("Browse (" + stackOverflowCacheFile + ")");
        this.expansionButton.setText("Browse (" + expansionFile + ")");
        this.stopWordButton.setText("Browse (" + stopWordFile + ")");
        this.stopWordButton.setText("Browse (" + stopWordFile + ")");
        this.readabilityClassifierButton.setText("Browse (" + readabilityClassifierFile + ")");
        this.dictionaryButton.setText("Browse (" + dictionaryFile + ")");
    }
}
