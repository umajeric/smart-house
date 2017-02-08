package si.majeric.android.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import si.majeric.smarthouse.wear.R;

public class TriToggleButton extends Button {
	private static final int DEFAULT_STATE = -1;

	// Keeps track of the current state, 0, 1, or 2
	private int _state;

	// Get the attributes created in attrs.xml
	private static final int[] STATE_ONE_SET = { R.attr.state_one };

	private static final int[] STATE_TWO_SET = { R.attr.state_two };

	private static final int[] STATE_THREE_SET = { R.attr.state_three };

	// Constructors
	public TriToggleButton(Context context) {
		super(context);
		_state = DEFAULT_STATE;
		this.setButtonText();
	}

	public TriToggleButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		_state = DEFAULT_STATE;
		this.setButtonText();
	}

	public TriToggleButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		_state = DEFAULT_STATE;
		this.setButtonText();
	}

	@Override
	public boolean performClick() {
		// Move to the next state
		// nextState();
		return super.performClick();
	}

	// Generate the drawable needed for the current state
	@Override
	public int[] onCreateDrawableState(int extraSpace) {
		// Add the number of states you have
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 3);

		if (_state == -1) {
			mergeDrawableStates(drawableState, STATE_ONE_SET);
		} else if (_state == 0) {
			mergeDrawableStates(drawableState, STATE_TWO_SET);
		} else if (_state == 1) {
			mergeDrawableStates(drawableState, STATE_THREE_SET);
		}

		return drawableState;
	}

	// Set current state, 0-2
	public void setState(int state) {
		if ((state > -1) && (state < 3)) {
			_state = state;
			setButtonText();
		}
	}

	public int getState() {
		return _state;
	}

	// Set the text displayed on the button
	private void setButtonText() {
		switch (_state) {
		case -1:
			this.setBackgroundResource(R.mipmap.icon_switch);
			break;
		case 0:
			this.setBackgroundResource(R.mipmap.icon_off);
			break;
		case 1:
			this.setBackgroundResource(R.mipmap.icon_on);
			break;
		default:
			this.setBackgroundResource(R.mipmap.icon_switch); // Should never happen, but just in case
			break;
		}
	}
}