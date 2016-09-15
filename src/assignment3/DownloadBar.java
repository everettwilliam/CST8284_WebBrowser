package assignment3;
/* **************************************************************
 * Algonquin College - School of Advanced Technology
 * CST 8284 - Object Oriented Programming
 * Assignment #3
 * 
 * Original Author: Eric Torunski
 * Author: EVERETT HOLDEN 
 * Student #: 040 812 130
 * Network login name: hold0052
 * Lab instructor: DAVE HOUTMAN
 * Section: 312
 * Due date: 2016.04.29
 * 
 * DownloadBar.java -- Class definition
 * 				   
 * Purpose -- Creates another window to view file download progress.
 * **************************************************************/
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/** DownloadBar
 *  Class
 *  Extends HBox class. Creates a window to show file download
 *  progress. Downloads files from network location.
 *  @author Eric Torunski
 * **************************************************************/
public class DownloadBar extends HBox {
	
	/** downloadWindow - a new window that display DownloadBar instances */
	private static Stage downloadWindow = null;
	/** downloadTasks - VBoc objects that hold DownloadBar instances  */
	private static VBox downloadTasks;	
	/** messageArea - displays test as to status of download */
	private static TextArea messageArea;
	/** progressBar - shows the current download progress */
	private static ProgressBar progressBar;
	/** fileNameDownloading - a Text object to display the file name downloading */
	private Text fileNameDownloading;
	/** urlLocation - the url location of the file downloading */
	private String urlLocation;
	/** cancel - a button to cancel and delete a file downloading */
	private Button cancel;
	
	
	/** getDownloadWindow
	 *  Constructor
	 *  Creates a new Stage object (window) that will display
	 *  file download tasks and their progress. 
	 *  Calling this function will guarantee that the downloadTasks VBox is created and visible.
	 *  @author Eric Torunski 
	 *  @return A Stage that will show each downloadTask's progress
	 * **************************************************************/
	public Stage getDownloadWindow() {
		if(downloadWindow == null)
		{
			//Create a new borderPane for the download window
			BorderPane downloadRoot = new BorderPane();
			downloadTasks = new VBox();			
			//downloadTasks will contain rows of DownloadTask objects, which are HBoxes
			downloadRoot.setCenter(downloadTasks);
			//The bottom of the window will be the message box for download tasks
			downloadRoot.setBottom(messageArea = new TextArea());
			downloadWindow = new Stage();
			Scene scene = new Scene(downloadRoot, 400, 600);
			//scene.getStylesheets().add("assignment3/style.css");
			downloadWindow.setScene(scene);
			downloadWindow.show();
			//When closing the window, set the variable downloadWindow to null
			downloadWindow.setOnCloseRequest(
					(event) ->{
						downloadWindow = null;
					});
		}
		return downloadWindow;
	}
	
	/** Download Bar
	 *  Constructor
	 *  Creates an instance of the DownloadBar class
	 *  @param newLocation  The String URL of a file to download
	 * **************************************************************/
	public DownloadBar(String newLocation){				

		urlLocation = newLocation;
		//See if the filename at the end of newLocation exists on your hard drive.
		// If the file already exists, then add (1), (2), ... (n) until you find a new filename that doesn't exist.
		String fileName = newLocation.substring(newLocation.lastIndexOf("/") + 1, newLocation.length());
		String filename[] = fileName.split("\\.");	
		int count = 0;
		while(fileCheck(fileName)){

			if(count > 0){
				fileName = filename[0] + "(" +	count + ")." + filename[1];	
			}					
			if(fileCheck(fileName)){
				count++;
			}
		}		
		
		//Create the window if it doesn't exist. After this call, the VBox and TextArea should exist.			
		getDownloadWindow();

		//Add a Text label for the filename
		fileNameDownloading = new Text(fileName);

		//Add a ProgressBar to show the progress of the task
		progressBar = new ProgressBar();
		progressBar.setProgress(0);
		progressBar.setPrefSize(250, 25);	
		//progressBar.getStyleClass().add("progressBar");
		
		//Add a cancel button that asks the user for confirmation, and cancel the task if the user agrees
		cancel = new Button("Cancel");			
				
		//adds download bar to downloadWindow
		addBar();
		
		//Start the download		
		DownloadTask aFileDownload = new DownloadTask();
		progressBar.progressProperty().bind(aFileDownload.progressProperty());
		new Thread(aFileDownload).start();	

		cancel.setOnAction(
				(event)->{
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setTitle("Cancel download");
					alert.setHeaderText("Do you want to cancel the download?");
					alert.setContentText("The file being downloaded will be deleted from the directory.");
					alert.showAndWait().ifPresent(
							(response)->{								
								if(response == ButtonType.OK){
									aFileDownload.cancel();
								}
							});
				});		
	}
	
	/** addBar
	 *  Method
	 *  Adds an instance of DownloadBar to downloadTasks VBox
	 * **************************************************************/
	private void addBar(){
		this.getChildren().addAll(fileNameDownloading, progressBar, cancel);
		downloadTasks.getChildren().add(this);
	}
	
	/** removeBar
	 *  Accessor Method
	 *  Removes the instance of DownloadBar to downloadTasks VBox
	 * **************************************************************/
	private void removeBar(){
		downloadTasks.getChildren().remove(this);
	}
	
	/** fileCheck
	 *  Method
	 *  Searches the download directory for the existence of a file
	 *  specified in the parameter.
	 *  @param fileName - the location of the file downloading
	 *  @return boolean - whether the file exists in the directory
	 * **************************************************************/
	private static final boolean fileCheck(String fileName){
		File download_dir = new File(MyGUI.getDownloadDir());
		String[] contents = download_dir.list();
		for(String file : contents){
			if(file.equals(fileName)){
				return true;
			}
		}		
		return false;
	}

	/**This class represents a task that will be run in a separate thread. It will run call(), 
	 *  and then call succeeded, cancelled, or failed depending on whether the task was cancelled
	 *  or failed. If it was not, then it will call succeeded() after call() finishes.
	 */
	/** DownloadTask
	 *  Inner Class
	 *  Extends Task class. Downloads file from network location.
	 * **************************************************************/
	private class DownloadTask extends Task<String>	{		

		private String saveFilePath = MyGUI.getDownloadDir() + File.separator + fileNameDownloading.getText();
		private InputStream inputStream;
		private FileOutputStream outputStream;
		private URL url;
		private HttpURLConnection httpConn;
		private int responseCode;
		private static final int BUFFER_SIZE = 4096;

		// This should start the download. Look at the downloadFile() function at:
		//  http://www.codejava.net/java-se/networking/use-httpurlconnection-to-download-file-from-an-http-url
		//Take that function but change it so that it updates the progress bar as it iterates through the while loop.
		// Here is a tutorial on how to upgrade a progress bar:
		// https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/progress.htm
		@Override
		protected String call() throws Exception {

			url = new URL(urlLocation);
			httpConn = (HttpURLConnection) url.openConnection();
			responseCode = httpConn.getResponseCode();				

			if(responseCode == HttpURLConnection.HTTP_OK){				 

				int contentLength = httpConn.getContentLength();
				int readSize = contentLength;					
				outputStream = new FileOutputStream(saveFilePath);
				inputStream = httpConn.getInputStream();

				byte[] buffer = new byte[BUFFER_SIZE];
				int bytesRead = 0;
				int byteCount = 0;

				while ( (bytesRead = inputStream.read(buffer)) >= 0){

					byteCount += bytesRead;

					if(isCancelled()){
						updateMessage("Cancelled");							
						break;
					}

					outputStream.write(buffer, 0, bytesRead);
					updateProgress(byteCount, readSize);
					Thread.sleep(50);
					byteCount++;
				}	
				outputStream.close();
			    inputStream.close();
				System.out.println("File downloaded");				
			}			
			httpConn.disconnect();
			return "Finished";
		}

		//Write the code here to handle a successful completion of the call() function.
		@Override
		protected void succeeded() {
			super.succeeded();	
			messageArea.appendText(fileNameDownloading.getText() + " was successfully downloaded.\n");
			removeBar();
		}

		//Write the code here to handle the task being cancelled before call() finishes.
		@Override
		protected void cancelled() {
			super.cancelled();
			messageArea.appendText(fileNameDownloading.getText() + ": download was canceled.\n");	
			removeBar();		
			String saveFilePath = MyGUI.getDownloadDir() + File.separator + fileNameDownloading.getText();
			File file = new File(saveFilePath);
			Path stuff = file.toPath();

			try {
				outputStream.close();
				inputStream.close();
				Files.deleteIfExists(stuff);

			} catch (NullPointerException|IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void failed() {	
			super.failed();	
			messageArea.appendText(fileNameDownloading.getText() + ": download failed!\n");	
			removeBar();			
			String saveFilePath = MyGUI.getDownloadDir() + File.separator + fileNameDownloading.getText();
			File file = new File(saveFilePath);
			Path stuff = file.toPath();

			try {
				outputStream.close();
				inputStream.close();
				Files.delete(stuff);

			} catch (NullPointerException|IOException e) {				
				e.printStackTrace();
			}			
		
		}
	}
}
