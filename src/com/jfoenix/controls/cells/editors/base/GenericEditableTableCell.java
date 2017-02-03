package com.jfoenix.controls.cells.editors.base;

import com.jfoenix.controls.cells.editors.TextFieldEditorBuilder;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;

import java.util.Objects;

/**
 * copy of {@link GenericEditableTreeTableCell}
 *
 * @author Shadi Shaheen
 * @author ci010
 */
public class GenericEditableTableCell<S, T> extends TableCell<S, T>
{
	protected EditorNodeBuilder builder;
	protected Region editorNode;

	public GenericEditableTableCell()
	{
		builder = new TextFieldEditorBuilder();
	}

	public GenericEditableTableCell(EditorNodeBuilder builder)
	{
		Objects.requireNonNull(builder);
		this.builder = builder;
	}

	@Override
	public void startEdit()
	{
		super.startEdit();
		if (editorNode == null)
			createEditorNode();
		builder.startEdit();
		setGraphic(editorNode);
		setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
	}

	@Override
	public void cancelEdit()
	{
		super.cancelEdit();
		builder.cancelEdit();
		builder.setValue(getValue());
		setContentDisplay(ContentDisplay.TEXT_ONLY);
		//Once the edit has been cancelled we no longer need the editor
		//so we mark it for cleanup here. Note though that you have to handle
		//this situation in the focus listener which gets fired at the end
		//of the editing.
		editorNode = null;
	}


	@Override
	public void updateItem(T item, boolean empty)
	{
		super.updateItem(item, empty);
		if (empty)
		{
			setText(null);
			setGraphic(null);
		}
		else
		{
			if (isEditing())
			{
				if (editorNode != null)
					builder.setValue(getValue());
				setGraphic(editorNode);
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				builder.updateItem(item, empty);
			}
			else
			{
				Object value = getValue();
				if (value instanceof Node)
				{
					setGraphic((Node) value);
					setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				}
				else
				{
					setText(value.toString());
					setContentDisplay(ContentDisplay.TEXT_ONLY);
				}
			}
		}
	}

	protected void commitHelper(boolean losingFocus)
	{
		if (editorNode == null) return;
		try
		{
			builder.validateValue();
			commitEdit(((T) builder.getValue()));
		}
		catch (Exception ex)
		{
			//Most of the time we don't mind if there is a parse exception as it
			//indicates duff user data but in the case where we are losing focus
			//it means the user has clicked away with bad data in the cell. In that
			//situation we want to just cancel the editing and show them the old
			//value.
			if (losingFocus)
				cancelEdit();
		}
	}

	private void createEditorNode()
	{
		EventHandler<KeyEvent> keyEventsHandler = t ->
		{
			if (t.getCode() == KeyCode.ENTER)
				commitHelper(false);
			else if (t.getCode() == KeyCode.ESCAPE)
				cancelEdit();
			else if (t.getCode() == KeyCode.TAB)
			{
				commitHelper(false);
				TableColumn nextColumn = getNextColumn(!t.isShiftDown());
				if (nextColumn != null)
					getTableView().edit(getIndex(), nextColumn);
			}
		};

		ChangeListener<Boolean> focusChangeListener = (observable, oldValue, newValue) ->
		{
			//This focus listener fires at the end of cell editing when focus is lost
			//and when enter is pressed (because that causes the text field to lose focus).
			//The problem is that if enter is pressed then cancelEdit is called before this
			//listener runs and therefore the text field has been cleaned up. If the
			//text field is null we don't commit the edit. This has the useful side effect
			//of stopping the double commit.
			if (!newValue && editorNode != null)
			{
				commitHelper(true);
			}
		};
		DoubleBinding minWidthBinding = Bindings.createDoubleBinding(() ->
				this.getWidth() - this.getGraphicTextGap() * 2 - this.getBaselineOffset(), this.widthProperty(), this.graphicTextGapProperty());
		editorNode = builder.createNode(getValue(), minWidthBinding, keyEventsHandler, focusChangeListener);
	}

	protected Object getValue()
	{
		return getItem() == null ? "" : getItem();
	}

	private TableColumn<S, ?> getNextColumn(boolean forward)
	{
		ObservableList<TableColumn<S, ?>> columns = getTableView().getColumns();
		if (columns.size() < 2) return null;
		int next = columns.indexOf(getTableColumn()) + (forward ? 1 : -1);
		next = next % columns.size();
		return columns.get(next);
	}
}
