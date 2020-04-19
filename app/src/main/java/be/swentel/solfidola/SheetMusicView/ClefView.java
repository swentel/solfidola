package be.swentel.solfidola.SheetMusicView;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import be.swentel.solfidola.R;

public class ClefView extends View
{
    // This function reads the attributes passed in through the XML and initializes the internal
    // noteData structure to the correct values
    // This function will throw an exception if its missing the noteValue and noteDuration attributes
    // or if they are incorrect values
    public ClefView(Context context, AttributeSet attributes)
    {
        super(context, attributes);
        initialize();
    }

    public ClefView(Context context)
    {
        super(context);
        initialize();
    }

    // Initialize the paint variables that will be used in onDraw()
    private void initialize()
    {
        int backgroundImageId;
        backgroundImageId = R.drawable.gclef;
        setBackgroundResource(backgroundImageId);

        // Set the background image to the center
        WindowManager.LayoutParams l = new WindowManager.LayoutParams();
        l.gravity = Gravity.CENTER;
        setLayoutParams(l);
    }
}
