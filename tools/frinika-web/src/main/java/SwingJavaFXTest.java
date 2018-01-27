
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.resource.Resource;

public class SwingJavaFXTest {

         private static void initAndShowGUI() {
             // This method is invoked on Swing thread
             JFrame frame = new JFrame("FX");
             final JFXPanel fxPanel = new JFXPanel();
             frame.add(fxPanel);
             frame.setVisible(true);
	     frame.setSize(600,400);
	     frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
             Platform.runLater(new Runnable() {
                 @Override
                 public void run() {
                     initFX(fxPanel);
                 }
             });
         }

	private static Scene createScene() {
	    Group  root  =  new  Group();
	    Scene  scene  =  new  Scene(root, Color.ALICEBLUE);
	    
	    WebView browser = new WebView();
	    WebEngine webEngine = browser.getEngine();
	    webEngine.load("http://localhost:15000/");
	    
	    root.getChildren().add(browser);

	    return (scene);
	}
    
        private static void initFX(JFXPanel fxPanel) {
             // This method is invoked on JavaFX thread
             Scene scene = createScene();
             fxPanel.setScene(scene);
         }

         public static void main(String[] args)  throws Exception {
	    Server server = new Server(15000);
	    HandlerCollection hc = new HandlerCollection();
	    
	    ResourceHandler rh = new ResourceHandler();
	    rh.setBaseResource(Resource.newClassPathResource("/com/frinika/web/content/"));
	    rh.setDirectoriesListed(true);
	    hc.addHandler(rh);
	    server.setHandler(hc);
	    server.start();
             SwingUtilities.invokeLater(new Runnable() {
                 @Override
                 public void run() {
                     initAndShowGUI();
                 }
             });
         }
    }