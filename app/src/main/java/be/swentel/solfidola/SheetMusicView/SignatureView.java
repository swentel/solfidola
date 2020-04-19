package be.swentel.solfidola.SheetMusicView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import be.swentel.solfidola.R;

public class SignatureView extends View
{

    private String type;
    private int note;

    // This function reads the attributes passed in through the XML and initializes the internal
    // noteData structure to the correct values
    // This function will throw an exception if its missing the noteValue and noteDuration attributes
    // or if they are incorrect values
    public SignatureView(Context context, AttributeSet attributes)
    {
        super(context, attributes);
        initialize();
    }

    public SignatureView(Context context, String type, int note)
    {
        super(context);
        this.type = type;
        this.note = note;
        initialize();
    }

    private void initialize()
    {
        int backgroundImageId;
        switch (this.type) {
            case "flat":
                backgroundImageId = R.drawable.flat;
                break;
            case "sharp":
            default:
                backgroundImageId = R.drawable.sharp;
                break;
        }
        setBackgroundResource(backgroundImageId);

        // Set the background image to the center
        WindowManager.LayoutParams l = new WindowManager.LayoutParams();
        l.gravity = Gravity.CENTER;
        setLayoutParams(l);
    }

    public int getNote() {
        return note;
    }
}
