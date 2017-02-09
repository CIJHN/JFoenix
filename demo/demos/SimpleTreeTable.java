package demos;

import com.jfoenix.controls.JFXTableView;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.cells.editors.base.GenericEditableTableCell;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.util.Comparator;

/**
 * @author ci010
 */
public class SimpleTreeTable extends Application
{

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		try
		{

			TableColumn<User, String> deptColumn = new TableColumn<>("Department");
			deptColumn.setPrefWidth(150);
			deptColumn.setResizable(false);
			deptColumn.setCellValueFactory(param -> param.getValue().department);

			TableColumn<User, String> empColumn = new TableColumn<>("Employee");
			empColumn.setPrefWidth(150);
			empColumn.setResizable(false);
			empColumn.setCellValueFactory((param) ->
			{
				return param.getValue().userName;
			});

			TableColumn<User, String> ageColumn = new TableColumn<>("Age");
			ageColumn.setPrefWidth(150);
			ageColumn.setResizable(false);
			ageColumn.setCellFactory(param -> new GenericEditableTableCell<>());
			ageColumn.setCellValueFactory(param ->
			{
				return param.getValue().age;
			});


//            ageColumn.setCellFactory((TreeTableColumn<User, String> param) -> new GenericEditableTreeTableCell<User, String>(new TextFieldEditorBuilder()));
//            ageColumn.setOnEditCommit((TreeTableColumn.CellEditEvent<User, String> t) -> {
//                ((User) t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow()).getValue()).age.set(t.getNewValue());
//            });
//
//            empColumn.setCellFactory((TreeTableColumn<User, String> param) -> new GenericEditableTreeTableCell<User, String>(new TextFieldEditorBuilder()));
//            empColumn.setOnEditCommit((TreeTableColumn.CellEditEvent<User, String> t) -> {
//                ((User) t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow()).getValue()).userName.set(t.getNewValue());
//            });
//
//            deptColumn.setCellFactory((TreeTableColumn<User, String> param) -> new GenericEditableTreeTableCell<User, String>(new TextFieldEditorBuilder()));
//            deptColumn.setOnEditCommit((TreeTableColumn.CellEditEvent<User, String> t) -> {
//                ((User) t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow()).getValue()).department.set(t.getNewValue());
//            });


			// data
			ObservableList<User> users = FXCollections.observableArrayList();
			users.add(new User("Computer Department", "23", "CD 1"));
			users.add(new User("Sales Department", "22", "Employee 1"));
			users.add(new User("Sales Department", "22", "Employee 2"));
			users.add(new User("Sales Department", "25", "Employee 4"));
			users.add(new User("Sales Department", "25", "Employee 5"));
			users.add(new User("IT Department", "42", "ID 2"));
			users.add(new User("HR Department", "22", "HR 1"));
			users.add(new User("HR Department", "22", "HR 2"));

			for (int i = 0; i < 40000; i++)
			{
				users.add(new User("HR Department", i % 10 + "", "HR 2" + i));
			}
			for (int i = 0; i < 40000; i++)
			{
				users.add(new User("Computer Department", i % 20 + "", "CD 2" + i));
			}

			for (int i = 0; i < 40000; i++)
			{
				users.add(new User("IT Department", i % 5 + "", "HR 2" + i));
			}

			// build tree

			FilteredList<User> filteredList = new FilteredList<>(users);
			SortedList<User> sorted = new SortedList<>(filteredList, Comparator.comparing(user -> user.userName.get()));

			JFXTableView<User> treeView = new JFXTableView<>(sorted);
			treeView.setEditable(true);
			treeView.setFixedSize(true);
			treeView.setColumnsDraggable(false);
			treeView.getColumns().setAll(deptColumn, ageColumn, empColumn);

			treeView.getStyleClass().add("tree-table-view");
			FlowPane main = new FlowPane();
			main.setPadding(new Insets(10));
			main.getChildren().add(treeView);

			sorted.comparatorProperty().bind(treeView.comparatorProperty());
//            JFXButton groupButton = new JFXButton("Group");
//            groupButton.setOnAction((action) -> {
//                new Thread(() -> treeView.group(empColumn)).start();
//            });
//            main.getChildren().add(groupButton);
//
//            JFXButton unGroupButton = new JFXButton("unGroup");
//            unGroupButton.setOnAction((action) -> treeView.unGroup(empColumn));
//            main.getChildren().add(unGroupButton);

			JFXTextField filterField = new JFXTextField();
			main.getChildren().add(filterField);


			filterField.textProperty().addListener((o, oldVal, newVal) ->
					filteredList.setPredicate(user -> user.age.get().contains(newVal) || user.department.get().contains(newVal) || user.userName.get().contains(newVal)));
//
//            size.textProperty().bind(Bindings.createStringBinding(() -> treeView.getCurrentItemsCount() + "", treeView.currentItemsCountProperty()));
//            main.getChildren().add(size);

			Scene scene = new Scene(main, 475, 500);
			scene.getStylesheets().add(SimpleTreeTable.class.getResource("/resources/css/jfoenix-components.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();

//						ScenicView.show(scene);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		launch(args);
	}

	class User extends RecursiveTreeObject<User>
	{

		StringProperty userName;
		StringProperty age;
		StringProperty department;

		public User(String department, String age, String userName)
		{
			this.department = new SimpleStringProperty(department);
			this.userName = new SimpleStringProperty(userName);
			this.age = new SimpleStringProperty(age);
		}

	}
}
