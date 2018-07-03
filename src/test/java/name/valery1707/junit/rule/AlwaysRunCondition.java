package name.valery1707.junit.rule;

import name.valery1707.junit.rule.ConditionalIgnoreRule.IgnoreCondition;

public class AlwaysRunCondition implements IgnoreCondition {
	@Override
	public boolean needRun() {
		return true;
	}
}
