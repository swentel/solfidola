# Solfidola

Solfège and more!

Features:

- Random interval training
- Create exercises with selected intervals
  - show bar or not
  - add random non selected interval
  -
- Use voice commands in exercises
  - play/replay: plays the interval again
  - speak the interval for the right solution (in English):
    unison (prime), minor second, major second, minor third (augmented second), major third,
    perfect fourth diminished fifth (augmented fourth, tritone), perfect fifth, minor sixth
    (augmented fifth), major sixth (diminished seventh), minor seventh, major seventh, octave

- Sound feedback when using the microphone
- Auto plays the interval and refreshes when the right solution is selected
- Tuner: still in experimental state (sing note C)

## Screenshot

<img src="https://realize.be/sites/default/files/solfidola-solfege.png?cache=1" width="400" />

## Development

To add new commands to be recognized, add new words to words-commands.txt and these commands:

- fstsymbols --save_osymbols=words.txt Gr.fst > /dev/null
- farcompilestrings --fst_type=compact --symbols=words.txt --keep_symbols words-commands.txt | ngramcount | ngrammake | fstconvert --fst_type=ngram > Gr.fst

## Credits

This app uses following external libraries:

- https://github.com/alphacep/vosk-api
- https://github.com/rodydavis/MidiDriver-Android-SF2
- https://github.com/kshoji/javax.sound.midi-for-Android
- https://github.com/JorenSix/TarsosDSP
- https://github.com/nitishp/SheetMusicView (with changes)
