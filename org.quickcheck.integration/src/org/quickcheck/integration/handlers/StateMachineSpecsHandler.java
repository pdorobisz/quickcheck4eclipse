package org.quickcheck.integration.handlers;

import java.util.ArrayList;

import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.internal.Workbench;
import org.erlide.core.erlang.ErlModelException;
import org.quickcheck.integration.ui.DynamicInputDialog;
import org.quickcheck.integration.ui.NullInputException;
import org.quickcheck.integration.utils.EditorUtils;
import org.quickcheck.integration.utils.InsertionStringPair;
import org.quickcheck.integration.utils.InsertionStringPair.Position;

public class StateMachineSpecsHandler extends AbstractQuickCheckHandler
		implements IHandler {

	@Override
	protected InsertionStringPair getInsertionString(String id)
			throws NullInputException, ErlModelException, BadLocationException {
		String before = "";
		String after = "";
		ArrayList<String> input;
		InsertionStringPair ret = null;
		if (id.equals("completeeqc_statemspec")) {
			before += getInsertionString("fullstatemmoduleheader").toString()
					+ EditorUtils.NEWLINE + EditorUtils.NEWLINE;
			String record = "";
			boolean useRecord = MessageDialog.openQuestion(Workbench
					.getInstance().getActiveWorkbenchWindow().getShell(),
					"Record usage", "Use a state record?");
			if (useRecord) {
				input = DynamicInputDialog.run("State record name",
						"Record name");
				record = input.get(0);
				before += "-record(" + record + EditorUtils.COMMA + "{})."
						+ EditorUtils.NEWLINE + EditorUtils.NEWLINE;
			}
			before += getInitialState(record) + EditorUtils.NEWLINE
					+ EditorUtils.NEWLINE;
			before += getInsertionString("command").toString()
					+ EditorUtils.NEWLINE + EditorUtils.NEWLINE;
			before += getInsertionString("next_state").toString()
					+ EditorUtils.NEWLINE + EditorUtils.NEWLINE;
			before += getInsertionString("precondition").toString()
					+ EditorUtils.NEWLINE + EditorUtils.NEWLINE;
			before += getInsertionString("postcondition").toString()
					+ EditorUtils.NEWLINE + EditorUtils.NEWLINE;
			input = DynamicInputDialog.run("State Machine Property",
					"Property name (prop_NAME)");
			before += getSMProperty(input.get(0), "?MODULE", true).toString();
		} else if (id.equals("fullstatemmoduleheader")) {
			before = getHeader();
			before += getIncludeEqc() + EditorUtils.NEWLINE;
			before += getIncludeEqcStatem() + EditorUtils.NEWLINE
					+ EditorUtils.NEWLINE;
			before += getCompileAll();
		} else if (id.equals("includeeqc_statem")) {
			before = getIncludeEqcStatem();
		} else if (id.equals("initial_state")) {
			before = getInitialState("");
		} else if (id.equals("command")) {
			before = "%% Command generator, S is the state"
					+ EditorUtils.NEWLINE;
			before += "command(S) ->" + EditorUtils.NEWLINE;
			before += EditorUtils.TAB + "oneof([" + EditorUtils.NEWLINE;
			before += EditorUtils.TAB + EditorUtils.TAB + "]).";
		} else if (id.equals("next_state")) {
			before = "%% Next state transformation, S is the current state"
					+ EditorUtils.NEWLINE;
			before += "next_state(S,_V,{call,_,_,_}) ->" + EditorUtils.NEWLINE;
			before += EditorUtils.TAB + "S.";
		} else if (id.equals("precondition")) {
			before = "%% Precondition, checked before command is added to the command sequence"
					+ EditorUtils.NEWLINE;
			before += "precondition(_S,{call,_,_,_}) ->" + EditorUtils.NEWLINE;
			before += EditorUtils.TAB + "true.";
		} else if (id.equals("postcondition")) {
			before = "%% Postcondition, checked after command has been evaluated"
					+ EditorUtils.NEWLINE;
			before += "%% OBS: S is the state before next_state(S,_,<command>)"
					+ EditorUtils.NEWLINE;
			before += "postcondition(_S,{call,_,_,_},_Res) ->"
					+ EditorUtils.NEWLINE;
			before += EditorUtils.TAB + "true.";
		} else if (id.equals("statemachineproperty")) {
			input = DynamicInputDialog.run("State Machine Property",
					"Property name (prop_NAME)",
					"Module for commands [?MODULE]");
			ret = getSMProperty(input.get(0), input.get(1), EditorUtils
					.getActiveSelectionLength() == 0);
		} else if (id.equals("symbolicfunctioncall")) {
			input = DynamicInputDialog.run("Smybolic function call",
					"Module name [?MODULE]", "Function name");
			before = "{call" + EditorUtils.COMMA + input.get(0)
					+ EditorUtils.COMMA + "[";
			after = "]}";
		} else if (id.equals("duplicatefunctionclause")) {
			String s = EditorUtils.getActiveFunctionClause();
			if (s.equals(null))
				throw new NullInputException();
			s = s.replace(".", ";");
			before += s;
			ret = new InsertionStringPair(before, "", Position.BEFORE);
		} else if (id.equals("duplicatefunctionclauseheader")) {
			String s = EditorUtils.getActiveFunctionClause();
			if (s.equals(null))
				throw new NullInputException();
			s = s.substring(0, s.indexOf("->") + 2);
			before += s;
			ret = new InsertionStringPair(before, "", Position.BEFORE);
		} else if (id.equals("commands1")) {
			EditorUtils.createMacro("commands/1", "Function commands/1",
					"Module");
		} else if (id.equals("commands2")) {
			EditorUtils.createMacro("commands/2", "Function commands/2",
					"Module", "Initial state");
		} else if (id.equals("more_commands")) {
			EditorUtils.compassWithMacro("more_commands",
					"Function more_commands", "Increase by factor");
		} else if (id.equals("run_commands2")) {
			EditorUtils.createMacro("run_commands/2",
					"Function run_commands/2", "Module", "List of commands");
		} else if (id.equals("run_commands3")) {
			EditorUtils.createMacro("run_commands/3",
					"Function run_commands/3", "Module", "Environment",
					"List of commands");
		} else if (id.equals("zip")) {
			EditorUtils.createMacro("zip", "Function zip", "List 1", "List 2");
		}
		if (ret != null)
			return ret;
		return new InsertionStringPair(before, after);
	}

	protected static String getInitialState(String record) {
		String before;
		before = "%% Initialize the state" + EditorUtils.NEWLINE;
		before += "initial_state() ->";
		before += EditorUtils.TAB + "#" + record + "{}.";
		return before;
	}

	protected static InsertionStringPair getSMProperty(String name,
			String module, boolean hasNotSelection) {
		String before;
		before = "prop_" + name + "() ->" + EditorUtils.NEWLINE;
		before += EditorUtils.TAB + "?FORALL(Cmds,commands(" + module + "),"
				+ EditorUtils.NEWLINE;
		before += EditorUtils.TAB + EditorUtils.TAB + "begin"
				+ EditorUtils.NEWLINE;
		before += EditorUtils.TAB + EditorUtils.TAB + EditorUtils.TAB
				+ "{H,S,Res} = run_commands(" + module + ",Cmds),"
				+ EditorUtils.NEWLINE;
		before += EditorUtils.TAB + EditorUtils.TAB + EditorUtils.TAB
				+ "?WHENFAIL(" + EditorUtils.NEWLINE;
		before += EditorUtils.TAB
				+ EditorUtils.TAB
				+ EditorUtils.TAB
				+ EditorUtils.TAB
				+ "io:format(\"History: ~p\\nState: ~p\\nRes: ~p\\n\",[H,S,Res]),";
		if (hasNotSelection)
			before += EditorUtils.NEWLINE + EditorUtils.TAB + EditorUtils.TAB
					+ EditorUtils.TAB + EditorUtils.TAB + "Res == ok";
		else
			before += EditorUtils.NEWLINE;
		String after = ")" + EditorUtils.NEWLINE;
		after += "end).";
		return new InsertionStringPair(before, after);
	}
}
