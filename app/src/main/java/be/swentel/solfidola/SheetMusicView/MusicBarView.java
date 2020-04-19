package be.swentel.solfidola.SheetMusicView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

// This class represents the music bar of the sheet view
// It contains a list of notes to display
// It also has a list of child NoteView's which for which it sets the positions
public class MusicBarView extends ViewGroup {

    private final int NUM_POSSIBLE_BLACK_AREA = 7;
    private final int NUM_DEFINITE_BLACK_AREA = 5;
    private final int NUM_WHITE_AREA = 6;
    private final float PERCENT_HEIGHT_BLACK_AREA = 0.01f;
    private final float PERCENT_HEIGHT_WHITE_AREA = (1 - (PERCENT_HEIGHT_BLACK_AREA*NUM_POSSIBLE_BLACK_AREA))/(NUM_WHITE_AREA);
    private final float PERCENT_NOTE_PADDING_LEFT = 0.2f;
    private final float PERCENT_NOTE_OVAL = 0.33f;
    private final int MAX_NUM_NOTES = 16;
    private final int NUM_WHITE_SPACES_LINE = 3; // number of white areas the child NoteView wants to cover

    private List<NoteData> notes;
    private List<Integer> signatures;
    private float width, height, xTopLeft;
    private List<Float> linePositions;
    private int leftStartValue = 350;

    private Paint musicBarBlack;

    public MusicBarView(Context context)
    {
        super(context);
        initialize(context);
    }

    public MusicBarView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initialize(context);
    }

    // Set up the parameters to initialize all paint objects
    private void initialize(Context context)
    {
        width = height = xTopLeft = 0;
        notes = new ArrayList<>();
        signatures = new ArrayList<>();
        musicBarBlack = new Paint();
        musicBarBlack.setColor(Color.BLACK);
        musicBarBlack.setStyle(Paint.Style.FILL);

        linePositions = new ArrayList<>();
        setWillNotDraw(false); // Make sure that onDraw is called for a viewGroup
    }

    @Override
    public void onDraw(Canvas canvas)
    {

        // Draw all the black areas
        for (int i = 0; i < NUM_DEFINITE_BLACK_AREA; ++i)
        {
            float yLineCenter = linePositions.get(i) + (musicBarBlack.getStrokeWidth() / 2);
            canvas.drawLine(xTopLeft, yLineCenter, xTopLeft + width, yLineCenter, musicBarBlack);
        }

        // Draw the left and right lines of the bar
        float xTopRight = xTopLeft + width;
        float yTop = linePositions.get(0);
        float yBottom = linePositions.get(NUM_DEFINITE_BLACK_AREA - 1);
        canvas.drawLine(xTopLeft, yTop, xTopLeft, yBottom, musicBarBlack);
        canvas.drawLine(xTopRight, yTop, xTopRight, yBottom,  musicBarBlack);

        // Draw the bottom lines as necessary for the notes
        float noteBegin = xTopLeft + getPaddingLeft();
        float noteWidth = this.width / getChildCount();

        // Since we have a clef and potential signature, the start of the first note
        // is not the beginning.
        if (getChildCount() == 6) {
            noteBegin += (noteWidth * 4) - leftStartValue;
        }
        else {
            noteBegin += noteWidth;
        }

        for(int i = 0; i < notes.size(); ++i)
        {
            float noteEnd = noteBegin + (this.width / MAX_NUM_NOTES);
            // Draw the first hidden line as necessary
            if(notes.get(i).getNoteValue().getValue() <= NoteData.NoteValue.LOWER_C.getValue())
            {
                float hiddenLineYVal = linePositions.get(NUM_POSSIBLE_BLACK_AREA - 1 - NoteData.NoteValue.LOWER_C.getValue());
                canvas.drawLine(noteBegin - (getPaddingLeft() / 2), hiddenLineYVal, noteEnd, hiddenLineYVal, musicBarBlack);
            }
            // Draw the second hidden line as necessary
            if(notes.get(i).getNoteValue().getValue() <= NoteData.NoteValue.LOWER_B.getValue())
            {
                float hiddenLineYVal = linePositions.get(NUM_POSSIBLE_BLACK_AREA - 1 - NoteData.NoteValue.LOWER_B.getValue());
                canvas.drawLine(noteBegin - (getPaddingLeft() / 2), hiddenLineYVal, noteEnd, hiddenLineYVal, musicBarBlack);
            }

            noteBegin += noteWidth;
        }
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom)
    {

        // Recompute all the dimensions and locations of the black lines
        setup();
        this.width = right - left - getPaddingRight() - getPaddingLeft();
        this.height = bottom - top - getPaddingBottom() - getPaddingTop();
        this.xTopLeft = getPaddingLeft();

        float blackLineHeight = PERCENT_HEIGHT_BLACK_AREA * this.height;
        musicBarBlack.setStrokeWidth(blackLineHeight);

        float whiteAreaHeight = PERCENT_HEIGHT_WHITE_AREA * this.height;
        float startVal = 0;
        for(int i = 0; i < NUM_POSSIBLE_BLACK_AREA; ++i)
        {
            linePositions.add(startVal);
            startVal += blackLineHeight + whiteAreaHeight;
        }

        // Don't bother setting up child widths if there are no children
        if (getChildCount() == 0)
            return;

        // Change the positions of the children
        int itemWidth = (int) (this.width / getChildCount());
        for(int i = 0, n = 0, s = 0; i < getChildCount(); ++i)
        {
            View v = getChildAt(i);

            // get the bottom value of the NoteView based on the NoteValue in the list of notes
            float incrementValue = 0.5f*(blackLineHeight + whiteAreaHeight);

            // ClefView.
            if (i == 0 && getChildCount() > 2) {
                v.layout(10, 0, 80, 190);
                continue;
            }

            // SignatureView.
            if (getChildCount() == 6 && i > 0 && i < 4) {
                int leftStartVal = 65 + (i * 35);
                int noteBottom = (int) (this.height - blackLineHeight - (signatures.get(s) * incrementValue)) + 3;
                v.layout(leftStartVal, noteBottom - v.getMeasuredHeight(), leftStartVal + v.getMeasuredWidth(), noteBottom);
                s++;
                continue;
            }

            int leftStartVal = (i * itemWidth) + getPaddingLeft() + (int) (this.width / MAX_NUM_NOTES * PERCENT_NOTE_PADDING_LEFT);
            if (getChildCount() == 6) {
                leftStartVal -= leftStartValue;
            }

            // Notes higher than B are laid out differently
            if((notes.get(n).getNoteDuration() == NoteData.NoteDuration.WHOLE)
                    || !notes.get(n).getNoteValue().greaterThanHigherB())
            {
                // most possible bottom value for the note is this.height - blackLineHeight (because the lowest note is LOWER_B)
                int noteBottom = (int) (this.height - blackLineHeight - (notes.get(n).getNoteValue().getValue() * incrementValue));
                v.layout(leftStartVal, noteBottom - v.getMeasuredHeight(), leftStartVal + v.getMeasuredWidth(), noteBottom);
            }
            else
            {
                int noteTop = (int) (this.height - blackLineHeight
                        - (notes.get(n).getNoteValue().getValue() * incrementValue)
                        - (v.getMeasuredHeight() * PERCENT_NOTE_OVAL));
                v.layout(leftStartVal, noteTop, leftStartVal + v.getMeasuredWidth(), noteTop + v.getMeasuredHeight());
            }

            n++;
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childWidth = (int) ((getMeasuredWidth() / MAX_NUM_NOTES) * (1 - (2 * PERCENT_NOTE_PADDING_LEFT)));
        int wSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY);
        int childHeight = (int) (getMeasuredHeight() * PERCENT_HEIGHT_WHITE_AREA * NUM_WHITE_SPACES_LINE);
        int hSpec = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY);

        // Set the height and width for every child
        for (int i = 0; i < getChildCount(); ++i)
        {
            if (getChildCount() > 2 && i == 0) {
                getChildAt(i).measure(wSpec, hSpec);
            }
            else if (getChildCount() == 6 && i > 0 && i < 4) {
                int childWidthS = 23;
                int wSpecS = MeasureSpec.makeMeasureSpec(childWidthS, MeasureSpec.EXACTLY);
                int childHeightS = 58;
                int hSpecS = MeasureSpec.makeMeasureSpec(childHeightS, MeasureSpec.EXACTLY);
                getChildAt(i).measure(wSpecS, hSpecS);
            }
            else {
                getChildAt(i).measure(wSpec, hSpec);
            }
        }
    }

    // Set up the notes list to make sure it matches with the child views of this MusicBarView
    // Check that each child view is a NoteView
    private void setup()
    {
        linePositions.clear(); // Need to do this to prevent a bunch of linePosition

        for (int i = 0, n = 0; i < getChildCount(); ++i) {
            NoteView noteView;
            SignatureView signatureView;
            String className = getChildAt(i).getClass().getSimpleName();

            switch (className) {
                case "ClefView":
                    // We only have one, so no need to store anything.
                    break;
                case "SignatureView":
                    // We only have one, so no need to store anything.
                    signatureView = (SignatureView) getChildAt(i);
                    signatures.add(signatureView.getNote());
                    break;
                case "NoteView":
                    noteView = (NoteView) getChildAt(i);
                    // Get the type attributes and initialize a new NoteData and modify the element in
                    // the list as needed
                    try {
                        notes.get(n).setNoteValue(noteView.getNoteValue());
                        notes.get(n).setNoteDuration(noteView.getNoteDuration());
                    }
                    catch (IndexOutOfBoundsException ignored) {
                        NoteData noteData = new NoteData(noteView.getNoteValue(), noteView.getNoteDuration());
                        notes.add(n, noteData);
                    }
                    n++;
                    break;
            }
        }
    }

}
