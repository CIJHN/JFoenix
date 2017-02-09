package com.jfoenix.controls;

import com.jfoenix.skins.JFXTableColumnHeader;
import com.jfoenix.skins.JFXTableRowSkin;
import com.sun.javafx.scene.control.skin.NestedTableColumnHeader;
import com.sun.javafx.scene.control.skin.TableColumnHeader;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import java.lang.reflect.Field;

/**
 * @author ci010
 */
public class JFXTableView<S> extends TableView<S>
{
	public JFXTableView()
	{
		super(FXCollections.observableArrayList());
		init();
	}

	public JFXTableView(ObservableList<S> itemSource)
	{
		super(itemSource);
		init();
	}

	private BooleanProperty columnsDraggable = new SimpleBooleanProperty(true);

	public boolean isColumnsDraggable()
	{
		return columnsDraggable.get();
	}

	public BooleanProperty columnsDraggableProperty()
	{
		return columnsDraggable;
	}

	public void setColumnsDraggable(boolean columnsDraggable)
	{
		this.columnsDraggable.set(columnsDraggable);
	}

	private BooleanProperty fixedSize = new SimpleBooleanProperty(false);

	public boolean isFixedSize()
	{
		return fixedSize.get();
	}

	public BooleanProperty fixedSizeProperty()
	{
		return fixedSize;
	}

	public void setFixedSize(boolean fixedSize)
	{
		this.fixedSize.set(fixedSize);
	}

	protected void init()
	{
		this.setRowFactory(new Callback<TableView<S>, TableRow<S>>()
		{
			@Override
			public TableRow<S> call(TableView<S> param)
			{
				return new TableRow<S>()
				{
					@Override
					protected Skin<?> createDefaultSkin()
					{
						return new JFXTableRowSkin<>(this);
					}
				};
			}
		});
		this.fixedSize.addListener(o -> layout());
		this.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	}

	@Override
	protected double computeMinWidth(double height)
	{
		if (!isFixedSize())
			return super.computeMinWidth(height);
		else
		{
			double w = 0;
			for (TableColumn<S, ?> column : this.getColumns())
				w += column.getWidth() + 5;
			return w;
		}
	}

	private static EventHandler<MouseEvent> mouseDraggedHandler;

	static
	{
		try
		{
			Field field = TableColumnHeader.class.getDeclaredField("mouseDraggedHandler");
			field.setAccessible(true);
			mouseDraggedHandler = (EventHandler<MouseEvent>) field.get(null);
		}
		catch (IllegalAccessException | NoSuchFieldException e)
		{
			mouseDraggedHandler = null;
			e.printStackTrace();
		}
	}

	@Override
	protected Skin<?> createDefaultSkin()
	{
		return new TableViewSkin<S>(this)
		{

			{
//				Callback<TableView<S>, TableRow<S>> callback = this.rowFactoryProperty().get();
//				this.rowFactoryProperty().set(param ->
//				{
//					TableRow<S> call = callback.call(param);
//					call.setOnMouseEntered(e -> JFXDepthManager.setDepth(call, 3));
//					call.setOnMouseExited(e -> JFXDepthManager.setDepth(call, 0));
//					return call;
//				});
			}

			@Override
			protected TableHeaderRow createTableHeaderRow()
			{
				TableViewSkin<S> skin = this;
				return new TableHeaderRow(this)
				{


					protected NestedTableColumnHeader createRootHeader()
					{
						return new NestedTableColumnHeader(skin, null)
						{

							protected TableColumnHeader createTableColumnHeader(TableColumnBase col)
							{
								return new JFXTableColumnHeader(getTableViewSkin(), col)
								{
									{
										this.onMouseDraggedProperty().bind(
												Bindings.createObjectBinding(() ->
																isColumnsDraggable() ? mouseDraggedHandler : null,
														columnsDraggable));
									}
								};
							}
						};
					}
				};
			}
		};
	}
}
