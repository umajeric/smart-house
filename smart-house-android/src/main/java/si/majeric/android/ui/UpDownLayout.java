package si.majeric.android.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.majeric.smarthouse.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class UpDownLayout extends LinearLayout {
	private static final Logger logger = LoggerFactory.getLogger(UpDownLayout.class);

	private Button _up;
	private Button _down;

	public UpDownLayout(Context context) {
		super(context);
		initInternal(context, null);
	}

	public UpDownLayout(Context context, AttributeSet attr) {
		super(context, attr);
		initInternal(context, attr);
	}

	protected final void initInternal(Context context, AttributeSet attr) {
		initializeLayoutBasics(context);
		registerButtons(context);
	}

	private void initializeLayoutBasics(Context context) {
		final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(getLayoutId(), this);
	}

	protected int getLayoutId() {
		return R.layout.updown_component;
	}

	protected void registerButtons(final Context context) {
		try {
			_up = (Button) findViewById(R.id.updownUpButton);
			_up.setBackgroundResource(android.R.drawable.arrow_up_float);
			_down = (Button) findViewById(R.id.updownDownButton);
			_down.setBackgroundResource(android.R.drawable.arrow_down_float);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	public void setOnUpClickListener(OnClickListener l) {
		_up.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					// increaseSize();
					return true;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					// resetSize();
					return true;
				}
				return false;
			}
		});
		if (_up != null) {
			_up.setOnClickListener(l);
		}
	}

	public void setOnDownClickListener(OnClickListener l) {
		if (_down != null) {
			_down.setOnClickListener(l);
		}
	}

}
