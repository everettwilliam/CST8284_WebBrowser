package assignment3;
/* **************************************************************
 * Algonquin College - School of Advanced Technology
 * CST 8284 - Object Oriented Programming
 * Assignment #3
 * 
 * Author: EVERETT HOLDEN 
 * Student #: 040 812 130
 * Network login name: hold0052
 * Lab instructor: DAVE HOUTMAN
 * Section: 312
 * Due date: 2016.04.29
 * 
 * MyGUI.java -- Class definition
 * 				   
 * Purpose -- Builds a web viewer GUI to view web pages
 * **************************************************************/

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Optional;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.Mnemonic;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.util.Duration;import javafx.util.Pair;


/** MyGUI
 *  Class: 
 *  A creates control object to form a web browser GUI.
 *  Extend the Application class.
 *  @author Everett Holden
 * **************************************************************/
public class MyGUI extends Application {

	/** engine - an instance of WebEngine to handle web documents */
	protected WebEngine engine;
	/** list - an observable list of WebHistory.Entry objects to track pages loaded in the engine*/
	protected ObservableList<WebHistory.Entry> list;	
	/** historyList - displays the contents of the ObservableList list */
	protected ListView<WebHistory.Entry> historyList;	
	/** back - a button for navigating back to the previously loaded web page in the history list.*/
	private Button back;
	/** forward - a button for navigating forward to a previously loaded web page in the history list.*/
	private Button forward;
	/** addBook - a button for adding a bookmark to the currently loaded web page*/
	private Button addBook;
	/** searchBar - a text field for inputing a url*/
	private TextField searchBar;
	/** bookmark - a menu field for selecting added bookmarks*/
	private Menu bookmark;

	//Objects that collect browser settings
	/** favorites - an array list of bookmark setting information*/
	private ArrayList<String> favorites = new ArrayList<>();
	/** homepage - the default webpage url loaded at runtime*/
	private String homepage;
	/** download_dir - the directory location of the downloads folder*/
	private static String download_dir;

	/** main
	 *  method: 
	 *  Calls launch method to start and instance of javaFX Application
	 *  @param args Command-line arguments	 * 
	 * **************************************************************/
	public static void main(String[] args) {
		launch(args);	
	}

	/** start
	 *  method: 
	 *  Contains the GUI control elements for the web browser application.  
	 *  @param primaryStage - the primary Stage of the FX Application
	 * **************************************************************/
	@Override
	public void start(Stage primaryStage) throws Exception {

		//menu bar and menu items
		BorderPane root = new BorderPane();		
		MenuBar menuBar = new MenuBar();		
		Menu file = new Menu("File");
		Menu settings = new Menu("Settings");

		//adds quit to file menu and adds event handler
		MenuItem quit = new MenuItem("Quit");
		MenuItem setHomepage = new MenuItem("Homepage");
		MenuItem setDownload = new MenuItem("Downloads");
		settings.getItems().addAll(setHomepage, setDownload);			

		//adds quit menu item to file menu
		file.getItems().add(quit);	
		//event handler for clicking on quit. Prompts user if they want to quit app.
		quit.setOnAction(
				(event)->{

					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setContentText("Does ya wanna quit?");
					alert.showAndWait().ifPresent( 
							(response)->{
								if(response == ButtonType.OK){
									saveSettings(primaryStage); //saves setting on close
									Platform.exit();								
								}
							});
				});	

		/* TEMPORARY MENU ********************************
		MenuItem download = new MenuItem("Download");
		file.getItems().add(download);

		download.setOnAction(
				(event)->{
					DownloadBar downloadBar = new DownloadBar("http://localhost:8081/TimeManagementWinter.pdf");
				});
		TEMPORARY MENU ********************************/

		//creates a menu item for the bookmark menu
		bookmark = new Menu("Bookmarks");		
		Menu help = new Menu("Help");
		MenuItem getHelp = new MenuItem("Get help for Java class");		

		//menu item the will toggle history view
		CheckMenuItem showHistory = new CheckMenuItem("Show History");

		//menu item about action handler
		MenuItem about = new MenuItem("About");
		//event handler for about menu item. Prompt display info to user.
		about.setOnAction(
				(event)->{

					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Information Dialog");
					alert.setHeaderText("About");
					alert.setContentText("Everett's browser, v1.0, Mar. 7, 2016");
					alert.showAndWait();	
				});
		//adds menu items to help menu in menu bar
		help.getItems().addAll(getHelp, showHistory, about);

		/* Assignment 3 - Accelerators *****************************************************************************/
		quit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
		getHelp.setAccelerator(new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN));
		about.setAccelerator(new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN));
		showHistory.setAccelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN));		
		setHomepage.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN));
		setDownload.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));	

		/* Assignment 3 - Homepage *********************************************************************************/
		//event handler for default homepage setting
		setHomepage.setOnAction(

				(event)->{

					Dialog<ButtonType> dialog = new Dialog<>();
					dialog.setTitle("Browser Settings");
					dialog.setHeaderText("Use this to set the browser's homepage.");					

					ButtonType ok = new ButtonType("OK", ButtonData.OK_DONE);
					ButtonType cancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

					dialog.getDialogPane().getButtonTypes().addAll(ok ,cancel);

					GridPane grid = new GridPane();
					grid.setHgap(10);
					grid.setVgap(10);
					grid.setPadding(new Insets(20, 100, 10, 10));

					TextField homepage = new TextField();
					homepage.setText(this.homepage);
					homepage.setPrefWidth(425);					

					grid.add(new Label("Homepage URL" ), 0, 0);					
					grid.add(homepage, 0, 1);									

					dialog.getDialogPane().setContent(grid);
					Optional<ButtonType> result = dialog.showAndWait();

					if(result.get().equals(ok)){
						if(!homepage.getText().equals("")){	
							setHomepage(homepage.getText());
						}else{
							setHomepage("Enter the url for the default homepage.");
						}
					}
				});

		/* Assignment 3 - Download*********************************************************************************/
		//event handler for download directory settings
		setDownload.setOnAction(

				(event)->{

					Dialog<ButtonType> dialog = new Dialog<>();
					dialog.setTitle("Browser Settings");
					dialog.setHeaderText("Use this to set the directory that files are downloaded to.");	

					ButtonType ok = new ButtonType("OK", ButtonData.OK_DONE);
					ButtonType cancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

					dialog.getDialogPane().getButtonTypes().addAll(ok ,cancel);

					GridPane grid = new GridPane();
					grid.setHgap(10);
					grid.setVgap(10);
					grid.setPadding(new Insets(20, 100, 10, 10));

					TextField text = new TextField();
					text.setText(getDownloadDir());
					text.setPrefWidth(425);					

					grid.add(new Label("Download directory" ), 0, 0);					
					grid.add(text, 0, 1);
					dialog.getDialogPane().setContent(grid);					
					Optional<ButtonType> result;

					int control = 0;
					while (control != -1){

						result = dialog.showAndWait();

						if(result.isPresent()){	

							if(result.get().equals(ok)){

								if(!text.getText().equals("")){

									setDownloadDir(text.getText());
								}else{
									setDownloadDir("Downloads");
								}

								File downloads = new File(getDownloadDir());

								if(!downloads.exists()){
									downloads.mkdir();
									control = -1;
								}else if(!Files.isWritable(downloads.toPath())){

									setDownloadDir("");
									Alert alert = new Alert(AlertType.WARNING);
									alert.setTitle("Downloads Directory");
									alert.setHeaderText("User does not have write permissions");
									alert.setContentText("The directory chosen cannot be written to because user does not have permission.");
									alert.showAndWait();

								}else{
									control = -1;
								}
							}
							if(result.get().equals(cancel)){
								control = -1;
							}
						}
					}
				});		

		//creates instance of inner class ClickHandler for handling button events
		ClickHandler response = new ClickHandler();

		//navigation menu bar
		HBox browserBar = new HBox();
		//creates buttons for navigation bar
		back = new Button("Back");		
		forward = new Button("Forward");		
		addBook = new Button("Add Bookmark");			
		//disables back and forward buttons
		back.setDisable(true);
		forward.setDisable(true);
		//adds event handler to buttons
		back.setOnMouseClicked(response);
		forward.setOnMouseClicked(response);
		addBook.setOnMouseClicked(response);
		//text field for url search
		searchBar = new TextField();
		searchBar.setOnMouseClicked(response);		

		/* Assignment 3 - adds tooltip to buttons ******************************************************************/
		back.setTooltip(new Tooltip("Click this button to navigate back one page."));
		forward.setTooltip(new Tooltip("Click this button to navigate forward one page."));
		addBook.setTooltip(new Tooltip("Click this button to save the current page to your bookmarks."));

		//adds navigation controls to HBox browserBar
		browserBar.getChildren().addAll(back, searchBar, addBook, forward);
		HBox.setHgrow(searchBar, Priority.ALWAYS);		
		//adds menus to menu bar
		menuBar.getMenus().addAll(file, bookmark, settings, help);
		//VBox hold the menuBar and the browser navigation bar
		VBox vbox = new VBox();
		vbox.getChildren().addAll(menuBar, browserBar);

		//browserView is an instance of WebView that manages the WebEngine object engine display
		WebView browserView = new WebView();

		/* Assignment 3 - hotkeys **********************************************************************************/
		//Sets shortcuts for CTRL+Left to go back and CTRL+Right to go forward 
		browserView.setOnKeyPressed(
				(event)->{

					if( event.getCode() == KeyCode.LEFT && event.isControlDown()){
						goBack();
					}
					if(event.getCode() == KeyCode.RIGHT && event.isControlDown()){
						goForward();
					}
				});

		//getEngine() returns the WebEngine object of browserView and sets it to engine
		engine = browserView.getEngine();

		//history side bar			
		list = FXCollections.observableArrayList();
		historyList = new ListView<WebHistory.Entry>(list);
		//sets the size of historyList to 0 so that it is not displayed by default
		historyList.setMaxSize(0,0);			

		//sets the top of the BorderPane region to be the menu and navigation bar
		root.setTop(vbox);
		//sets the right of the BorderPane region to be the history navigation list
		root.setRight(historyList);	
		//sets the center of the BorderPane region to be the WebView object
		root.setCenter(browserView);
		root.getCenter().autosize();

		//event handler that reads settings file after the primary stage is shown
		primaryStage.setOnShown(
				(event)->{
					readSettings(primaryStage);
					browserView.getEngine().load(homepage);
				});	

		Scene scene = new Scene(root, 800, 600);
		primaryStage.setTitle("Assignment 2");
		primaryStage.setScene(scene);		
		primaryStage.show();

		//This is a 3-parameter Lambda function for listening for changes
		// of state for the web page loader.				
		engine.getLoadWorker().stateProperty().addListener(

				( ov, oldState,  newState)-> {

					// This if statement gets run if the new page load succeeded.
					if (newState == State.SUCCEEDED) {		

						//sets the WebHistory list to items from WebEngine history 
						list.setAll(engine.getHistory().getEntries());

						//displays current url in searchBar textField
						searchBar.setText( engine.getLocation());

						//enables or disables the back button depending on page history index
						if(list.size() >= 1 && engine.getHistory().getCurrentIndex() != 0){
							back.setDisable(false);
						}else{
							back.setDisable(true);
						}
						//enables or disables the forward button depending on page history index
						if(engine.getHistory().getCurrentIndex() < (list.size() - 1 ) ){
							forward.setDisable(false);				
						}else {
							forward.setDisable(true);
						}						
						//enables or disables bookmark button depending on whether current url is an id for
						//MenuItem in observable list for Menu						
						for(MenuItem trash : bookmark.getItems()){
							//diagnostic print
							//System.out.println(trash.getId().equals(engine.getLocation()));
							//System.out.println("ID in list: " + trash.getId());
							//System.out.println("Location: " + engine.getLocation());

							if( trash.getId().equals(engine.getLocation())){
								addBook.setDisable(true);
								break;
							}
							addBook.setDisable(false);	
						}					
					}
				});

		//event handler for history list. Allows selection to be made.
		historyList.getSelectionModel().selectedIndexProperty().addListener(

				(ov, oldIDX , newIDX)->{

					int selectedIndex = newIDX.intValue();
					int currentIndex = engine.getHistory().getCurrentIndex();
					int difference = selectedIndex - currentIndex;
					if(selectedIndex != -1){
						//diagnostic print
						//System.out.println(difference);
						//tells the WebEngine to go forward or back in its history
						engine.getHistory().go(difference);
					}

				});

		//shows history list when 'show history' menu is checked
		showHistory.setOnAction(
				(event) ->{								
					FadeTransition ft = new FadeTransition(Duration.millis(1500), root.getRight());
					ScaleTransition st = new ScaleTransition(Duration.millis(750), root.getRight());
					RotateTransition rt = new RotateTransition(Duration.millis(750), root.getRight());
					ParallelTransition pt = new ParallelTransition(st, ft);	
					SequentialTransition seq1 = new SequentialTransition(pt, rt);
					SequentialTransition seq2 = new SequentialTransition(rt, pt);								

					if(showHistory.isSelected()){
						ft.setToValue(1f);
						ft.setFromValue(0f);						
						ft.setCycleCount(1);
						st.setFromX(0f);
						st.setFromY(0f);
						st.setToX(1f);
						st.setToY(1f);
						st.setCycleCount(1);								
						rt.setByAngle(-360);
						seq1.play();
						historyList.setMaxSize(500, 700);

					}else {													
						ft.setToValue(0f);
						ft.setFromValue(1f);						
						ft.setCycleCount(1);
						st.setFromX(1f);
						st.setFromY(1f);
						st.setToX(0f);
						st.setToY(0f);
						st.setCycleCount(1);								
						rt.setByAngle(360);	
						seq2.play();
						seq2.setOnFinished(
								e ->{
									historyList.setMaxSize(0, 0);
								});
					}
				});			

		//event handler to accept string typed in search bar when enter is keyed
		searchBar.setOnKeyPressed(
				(event)->{
					if(event.getCode() == KeyCode.ENTER){
						String http = searchBar.getText();
						System.out.println(searchBar.getText());
						browserView.getEngine().load(http.toString());											
					}
				});	

		//event handler for get help menu item. Allows user to search google by typing into a prompt.		
		getHelp.setOnAction(
				(event)->{
					//creates prompt for user to enter search terms.
					TextInputDialog dialog = new TextInputDialog();
					dialog.setTitle("Find help for Java Class");
					dialog.setHeaderText("Search for Java Class documentation");
					dialog.setContentText("Which Java class do you want to research?");
					Optional<String> result = dialog.showAndWait();
					if(result.isPresent()){
						//search terms are inserted after a google search url.
						String http = "https://www.google.ca/search?q=JavaFX+" + result.get();
						browserView.getEngine().load(http);
						System.out.println(http);							
					}
				});


		/* Assignment 3 - saveSettings **********************************************************************************/
		//event handler that writes setting to file after primary stage is closed
		primaryStage.setOnCloseRequest(
				(event)->{
					saveSettings(primaryStage);
				});	

		/* Assignment 3 - download***************************************************************************************/
		// monitor the location url, and if newLoc ends with one of the download file endings, create a new DownloadTask.
		engine.locationProperty().addListener(new ChangeListener<String>() {
			@Override public void changed(ObservableValue<? extends String> observableValue, String oldLoc, String newLocation) {
				if(newLocation.endsWith(".exe") || 
						newLocation.endsWith(".pdf") ||
						newLocation.endsWith(".txt") ||
						newLocation.endsWith(".zip") ||
						newLocation.endsWith(".doc") ||
						newLocation.endsWith(".docx") ||
						newLocation.endsWith(".xls") ||
						newLocation.endsWith(".xlsx") ||
						newLocation.endsWith(".iso") ||
						newLocation.endsWith(".img") ||
						newLocation.endsWith(".dmg") ||
						newLocation.endsWith(".tar") ||
						newLocation.endsWith(".tgz") ||
						newLocation.endsWith(".jar") 
						){		

					DownloadBar newDownload = new DownloadBar(newLocation);	
					browserView.getEngine().load("blank");
				}
			}
		});

	}

	/** ClickHandler
	 *  Inner class: 
	 *  Handles events for buttons and text field.
	 * **************************************************************/
	private class ClickHandler implements EventHandler<MouseEvent>{		

		@Override
		public void handle(MouseEvent event) {
			//reference type control is superclass of button and textfield
			//this allows click to be used for buttons and texfields
			Control click = (Control)event.getSource();					

			if(click == back){		
				goBack();//event handler for back button. Calls goBack()
			}else if( click == forward){
				goForward();//event handler for forward button. Calls goForward()				
			}else if(click == addBook){////event handler for bookmark button.
				//saves current location in WebEngine as a String
				String url = engine.getLocation();
				//creates a new MenuItem to represent a bookmark
				MenuItem newItem = new MenuItem();
				//sets the MenuItem's id to the current url
				newItem.setId(url);
				//creates a text prompt so the user can label the new MenuItem
				newItem.setText(engine.getTitle());
				System.out.println(engine.getTitle());				
				bookmark.getItems().add(newItem);										
				addBook.setDisable(true);			
				//diagnostic print
				System.out.println(newItem.toString());

				/* ** Assign 3 requirement */
				String [] array = newItem.toString().split("\\[");
				String bookString = "title=" + newItem.getText() + ", " + array[1] + "[" + array[2];
				bookString = bookString.substring(0, bookString.length() - 1);
				favorites.add(bookString);

				//loads url associated with MenuItem (id) and loads into WebEngine
				newItem.setOnAction(
						(stuff)->{								
							engine.load(url);								
						});
			}else if(click == searchBar){//event handler for text field bar.
				if(event.getClickCount() == 2){
					searchBar.setText("");//clears text from search bar
				}else if(event.getClickCount() == 1){
					searchBar.selectAll();//highlights text in search bar
				}
			}							
		}	
	}

	/** goBack
	 *  method: 
	 *  Tell the web engine to go back 1 page in the history
	 *  @author Eric Torunski
	 * **************************************************************/
	public void goBack(){
		
		final WebHistory history=engine.getHistory();
		ObservableList<WebHistory.Entry> entryList=history.getEntries();
		int currentIndex=history.getCurrentIndex();

		if(currentIndex > 0){
			//This is a no-parameter Lambda function run();
			Platform.runLater( () -> { 
				history.go(-1); 
				final String nextAddress = history.getEntries().get(currentIndex - 1).getUrl();
			});
		}
	}

	/** goForward
	 *  method: 
	 *  Tell the web engine to go forward 1 page in the history
	 *  @author Eric Torunski
	 * **************************************************************/
	public void goForward(){

		final WebHistory history=engine.getHistory();
		ObservableList<WebHistory.Entry> entryList=history.getEntries();
		int currentIndex=history.getCurrentIndex();

		if(currentIndex + 1 < entryList.size()){	
			//This is a no-parameter Lambda function run();
			Platform.runLater( () -> { 
				history.go(1); 
				final String nextAddress = history.getEntries().get(currentIndex + 1).getUrl();
			});
		}    
	}
	
	/** getBookmarkMenu
	 *  Accessor Method
	 *  @return bookmark - Menu object bookmark
	 * **************************************************************/
	public Menu getBookmarkMenu(){
		return bookmark;
	}

	/** setFavorites
	 *  Mutator Method
	 *  @param favorites - Array list of bookmark info
	 * **************************************************************/
	public void setFavorites(ArrayList<String> favorites){
		this.favorites = favorites;		
	}

	/** setHomepage
	 *  Mutator Method
	 *  @param homepage - the default webpage loaded at start
	 * **************************************************************/
	public void setHomepage(String homepage){
		this.homepage = homepage;
	}

	/** setDownloadDir
	 *  Mutator Method
	 *  @param directory - the location of the download directory
	 * **************************************************************/
	public void setDownloadDir(String directory){
		MyGUI.download_dir = directory;
	}

	/** getDownLoadDir
	 *  Accessor Method
	 *  @return download_dir - the location of the download directory
	 * **************************************************************/
	public static String getDownloadDir(){
		return download_dir;
	}

	/** saveSettings
	 *  Method
	 *  Saves bookmark MenuItems to file as a binary stream.	  
	 *  Saves various settings to file as text.
	 *  @param primaryStage - the primary stage of the javaFX start method
	 * **************************************************************/
	public void saveSettings(Stage primaryStage){

		File bookmarks = new File("./bookmarks");
		File settings = new File("./settings.txt");

		try(ObjectOutputStream saveBook = new ObjectOutputStream(new FileOutputStream(bookmarks))){			

			saveBook.writeObject(favorites);
			saveBook.close();

		}catch (IOException e){
			e.printStackTrace();
		}

		try(BufferedWriter writer = new BufferedWriter(new FileWriter(settings))){

			String screenX = "screenX=" + Double.toString(primaryStage.getX());
			String screenY = "screenY=" + Double.toString(primaryStage.getY());			
			String height = "height=" + Double.toString(primaryStage.getHeight());
			String width = "width=" +Double.toString(primaryStage.getWidth());
			String download_dir = getDownloadDir();			

			writer.write(screenX);
			writer.newLine();
			writer.write(screenY);
			writer.newLine();
			writer.write(height);
			writer.newLine();
			writer.write(width);
			writer.newLine();
			writer.write("homepage=" + homepage);
			writer.newLine();
			writer.write("download_dir=" + download_dir);
			writer.newLine();

		}catch (IOException e){
			e.printStackTrace();
		}

	}

	/** readSettings
	 *  Method
	 *  Reads bookmark file and reads in saved bookmarks.
	 *  Recreates bookmark MenuItems.
	 *  Reads settings files and loads various settings.
	 *  @param primaryStage - the primary stage of the main scene
	 * **************************************************************/
	public void readSettings(Stage primaryStage){

		File bookmarks = new File("./bookmarks");
		File settings = new File("./settings.txt");

		try(ObjectInputStream readBook = new ObjectInputStream(new FileInputStream(bookmarks))){

			if(bookmarks.exists()){
				setFavorites((ArrayList<String>) readBook.readObject());
				readBook.close();
			}else{
				System.out.println("File not found.");
			}


			for( String bookmark : favorites){	

				MenuItem newItem = new MenuItem();
				getBookmarkMenu().getItems().add(newItem);										
				addBook.setDisable(true);
				bookmark = bookmark.trim();				
				String [] line = bookmark.split("\\, ");

				for(int i = 0; i < line.length; i++){

					if(line[i].contains("title")){
						line[i] = line[i].substring(line[i].indexOf("=") + 1, line[i].length());
						newItem.setText(line[i]);

					}else if(line[i].contains("id")){
						line[i] = line[i].substring(line[i].indexOf("=") + 1, line[i].length());
						String url = line[i];
						newItem.setId(line[i]);
						newItem.setOnAction(
								(stuff)->{								
									engine.load(url);								
								});

					}else if(line[i].contains("style")){
						line[i] = line[i].substring(line[i].indexOf("=") + 1, line[i].length());
						newItem.setStyle(line[i]);
					}
				}	
			}

		}catch (IOException | ClassNotFoundException e){
			e.printStackTrace();
		}

		try(BufferedReader reader = new BufferedReader(new FileReader(settings))){

			if(settings.exists()){

				String line = "";

				while(line != null){

					line = reader.readLine();

					if(line != null){

						line = line.trim();

						if(line.contains("=")){

							String [] param = line.split("\\=");

							switch(param[0]){						

							case "screenX":
								primaryStage.setX(Double.parseDouble(param[1]));
								break;
							case "screenY":
								primaryStage.setY(Double.parseDouble(param[1]));
								break;
							case "height":
								primaryStage.setHeight(Double.parseDouble(param[1]));
								break;
							case "width":
								primaryStage.setWidth(Double.parseDouble(param[1]));
								break;
							case "download_dir":							
								if(param.length == 1){
									setDownloadDir("");
								}else{
									setDownloadDir(param[1]);
								}
								break;
							case "homepage":
								if(param.length == 1){
									setHomepage("");
								}else{
									setHomepage(param[1]);
								}

								break;
							default:
								break;
							}
						}
					}
				}

				reader.close();
			}

		}catch (IOException e){
			e.printStackTrace();
		}

	}


}
