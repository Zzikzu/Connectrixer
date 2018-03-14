package sample;

import io.UserProperties;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class UserDialogController {

    @FXML
    private TextField userName;

    @FXML
    private Spinner sessionCount = new Spinner();
    private SpinnerValueFactory.IntegerSpinnerValueFactory sessionCountFactory;

    @FXML
    private Spinner tabCount = new Spinner();
    private SpinnerValueFactory.IntegerSpinnerValueFactory tabCountFactory;

    @FXML
    private PasswordField password;

    @FXML
    private CheckBox o2CheckBox;

    public void initialize(){
        sessionCountFactory = (SpinnerValueFactory.IntegerSpinnerValueFactory) sessionCount.getValueFactory();
        tabCountFactory = (SpinnerValueFactory.IntegerSpinnerValueFactory) tabCount.getValueFactory();
        sessionCountFactory.setMax(15);
        sessionCountFactory.setMin(1);
        sessionCountFactory.setValue(6);
        tabCountFactory.setMax(30);
        tabCountFactory.setMin(1);
        tabCountFactory.setValue(3);

        String login = UserProperties.getInstance().getLogin();
        if (login != null){
            userName.setText(login);
        }

        sessionCountFactory.setValue(UserProperties.getInstance().getSessionCount());
        tabCountFactory.setValue(UserProperties.getInstance().getTabCount());
        password.setText(UserProperties.getInstance().getPassword());
        o2CheckBox.setSelected(UserProperties.getInstance().getO2Selected());
    }

    String getUserName(){
        return userName.getText();
    }

    int getSessionCount(){
        return sessionCountFactory.getValue();
    }

    int getTabCount(){
        return tabCountFactory.getValue();
    }

    String getPassword() {
        return password.getText();
    }

    public CheckBox getO2CheckBox() {
        return o2CheckBox;
    }

}
