package si.majeric.smarthouse.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Cron implements Serializable {
	private static final long serialVersionUID = 1L;
	private String expression;
	private Boolean manualTriggerEnabled = Boolean.TRUE;

	public Cron() {
	}

	/**
	 * 
	 * @return expression that triggers particular job.<br/>
	 * @see <a href="http://www.quartz-scheduler.org/documentation/quartz-2.2.x/tutorials/tutorial-lesson-06">CronTrigger Tutorial</a> for details.
	 */
	@Column(name = "cron_expression")
	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	/**
	 * 
	 * @return true if manual triggering is still enabled and as such shown on the UI, false otherwise.
	 */
	@Column(name = "cron_man_trigger_enabled")
	public Boolean getManualTriggerEnabled() {
		return manualTriggerEnabled;
	}

	public void setManualTriggerEnabled(Boolean manualTriggerEnabled) {
		this.manualTriggerEnabled = manualTriggerEnabled;
	}

	@Override
	public String toString() {
		return Cron.class.getSimpleName() + "('" + expression + "';" + manualTriggerEnabled + ")";
	}
}
