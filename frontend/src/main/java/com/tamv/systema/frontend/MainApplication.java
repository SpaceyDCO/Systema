package com.tamv.systema.frontend;

import com.tamv.systema.frontend.API.ApiService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
    private ApiService apiService;
    @Override
    public void start(Stage stage) throws Exception {
        this.apiService = new ApiService();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/tamv/systema/frontend/login-view.fxml"));
        fxmlLoader.setControllerFactory(controllerClass -> {
            if(controllerClass == LoginController.class) {
                return new LoginController(apiService);
            }else {
                try {
                    return controllerClass.getDeclaredConstructor().newInstance();
                }catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        Scene scene = new Scene(fxmlLoader.load(), 600, 195);
        stage.setTitle("Systema login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public static void showMainWindow(Stage oldStage, String username, ApiService api) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("/com/tamv/systema/frontend/main-view.fxml"));
            Scene scene = new Scene(loader.load());
            MainController controller = loader.getController();
            controller.initializeWithUsername(username);
            controller.setApi(api);
            Stage mainStage = new Stage();
            mainStage.setTitle("Systema - Main Dashboard");
            mainStage.setScene(scene);
            oldStage.close();
            mainStage.show();
        }catch(IOException e) {
            e.printStackTrace();
        }
    }
}
