package client;

import client.controller.ClientController;
import client.model.GameState;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import javafx.stage.Window;

public class MainClientFX extends Application {

    private GameState gameState;

    private Label lblPlayer1Time;
    private Label lblPlayer2Time;
    private Label lblPlayer1Name;
    private Label lblPlayer2Name;

    private Circle avatar1;
    private Circle avatar2;

    private VBox p1Box;
    private VBox p2Box;
    private Button[][] cells;
    private StackPane[][] cellPanes;

    private Canvas drawLayer;
    private double cellSize;
    private Stage primaryStage;
    private ClientController controller;

    private Timeline timeline;
    private Stage loadingDialogStage;

    // Quản lý dialog đang mở để đóng khi cần
    private Stage activeDialog;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.gameState = new GameState();
        this.controller = new ClientController(gameState, this);

        Scene homeScene = createHomeScene(stage);
        stage.setTitle("Caro FX - Networking Mode");
        stage.setScene(homeScene);
        stage.setWidth(900);
        stage.setHeight(700);
        stage.centerOnScreen();
        stage.show();
    }

    public void closeAllDialogs() {
        if (activeDialog != null) {
            activeDialog.close();
            activeDialog = null;
        }
    }

    public void switchToGameScene() {
        if (primaryStage != null) {
            Scene gameScene = createGameScene(primaryStage);

            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), primaryStage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                primaryStage.setScene(gameScene);
                primaryStage.setMaximized(true);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), gameScene.getRoot());
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
                startTimer();
            });
            fadeOut.play();
        }
    }

    public void updatePlayerNames(String player1Name, String player2Name) {
        if (lblPlayer1Name != null) lblPlayer1Name.setText(player1Name);
        if (lblPlayer2Name != null) lblPlayer2Name.setText(player2Name);
    }

    private Scene createHomeScene(Stage stage) {
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setBackground(new Background(new BackgroundFill(Color.web("#0A0E1A"), CornerRadii.EMPTY, Insets.EMPTY)));

        VBox mainContainer = new VBox(40);
        mainContainer.setPadding(new Insets(60, 80, 60, 80));
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setMaxWidth(600);

        Label title = new Label("CARO");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 72));

        Label subtitle = new Label("Trò chơi cờ caro 5 nước thắng");
        subtitle.setTextFill(Color.web("#B0B0B0"));
        subtitle.setFont(Font.font("Arial", 16));

        VBox titleBox = new VBox(12);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.getChildren().addAll(title, subtitle);

        javafx.scene.shape.Line separator = new javafx.scene.shape.Line(0, 0, 400, 0);
        separator.setStroke(Color.web("#4A90E2"));
        separator.setStrokeWidth(2);

        VBox inputSection = new VBox(16);
        inputSection.setAlignment(Pos.CENTER);

        Label lblName = new Label("Nhập tên của bạn");
        lblName.setTextFill(Color.WHITE);
        lblName.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        TextField txtName = new TextField();
        txtName.setPromptText("Nhập tên người chơi...");
        txtName.setStyle("-fx-font-size: 16; -fx-padding: 16; -fx-background-color: #1A1F2E; -fx-text-fill: white; -fx-prompt-text-fill: #888888; -fx-border-color: #4A90E2; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");
        txtName.setPrefHeight(50);
        txtName.setPrefWidth(300);

        inputSection.getChildren().addAll(lblName, txtName);

        Button btnPlay = new Button("CHƠI NGAY");
        btnPlay.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-padding: 16 60 16 60; -fx-background-color: #2D89EF; -fx-text-fill: white; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;");
        btnPlay.setPrefHeight(50);

        btnPlay.setOnAction(e -> {
            // --- SỬA LỖI TẠI ĐÂY ---
            // 1. Vô hiệu hóa nút ngay lập tức để tránh double-click
            btnPlay.setDisable(true);

            String name = txtName.getText();
            if (name == null || name.trim().isEmpty()) {
                showCustomWarningDialog("Cảnh báo", "Vui lòng nhập tên người chơi.");
                btnPlay.setDisable(false); // Mở lại nút nếu nhập liệu sai
                return;
            }

            // 2. Tạo một bộ đếm ngược an toàn: Nếu sau 5 giây mà chưa vào được game
            // (do lỗi mạng server không phản hồi) thì mở lại nút để bấm lại.
            PauseTransition failSafe = new PauseTransition(Duration.seconds(5));
            failSafe.setOnFinished(event -> btnPlay.setDisable(false));
            failSafe.play();

            controller.onPlayNow(name.trim());
        });

        mainContainer.getChildren().addAll(titleBox, separator, inputSection, btnPlay, new Label("Chơi ngay để thử thách bản thân"));
        root.getChildren().add(mainContainer);
        return new Scene(root, 900, 700);
    }

    private Scene createGameScene(Stage stage) {
        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(Color.web("#121621"), CornerRadii.EMPTY, Insets.EMPTY)));

        HBox top = new HBox(8);
        top.setPadding(new Insets(10, 16, 10, 16));
        top.setAlignment(Pos.CENTER);
        top.setBackground(new Background(new BackgroundFill(Color.web("#121621"), CornerRadii.EMPTY, Insets.EMPTY)));

        p1Box = createPlayerCard(gameState.getPlayer1Name(), 'X', Color.web("#FF4444"), Color.web("#CC0000"));
        p1Box.setId("player1Card");
        VBox centerSection = createCenterSection();
        p2Box = createPlayerCard(gameState.getPlayer2Name(), 'O', Color.web("#50C878"), Color.web("#3FA568"));
        p2Box.setId("player2Card");

        HBox.setHgrow(p1Box, Priority.ALWAYS);
        HBox.setHgrow(centerSection, Priority.NEVER);
        HBox.setHgrow(p2Box, Priority.ALWAYS);
        p1Box.setMaxWidth(Double.MAX_VALUE);
        p2Box.setMaxWidth(Double.MAX_VALUE);
        top.getChildren().addAll(p1Box, centerSection, p2Box);
        root.setTop(top);

        int size = gameState.getBoardSize();
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(16));
        grid.setHgap(2);
        grid.setVgap(2);
        grid.setAlignment(Pos.CENTER);
        grid.setBackground(new Background(new BackgroundFill(Color.web("#181F2E"), CornerRadii.EMPTY, Insets.EMPTY)));

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double availableHeight = screenBounds.getHeight() - 220;
        this.cellSize = Math.min(40, Math.floor(availableHeight / size) - 2);
        this.cellSize = Math.max(20, this.cellSize);

        cells = new Button[size][size];
        cellPanes = new StackPane[size][size];

        this.drawLayer = new Canvas(800, 800);
        drawLayer.setMouseTransparent(true);

        StackPane gridWithOverlay = new StackPane();
        gridWithOverlay.setAlignment(Pos.CENTER);
        DropShadow gridShadow = new DropShadow();
        gridShadow.setColor(Color.color(0, 0, 0, 0.3));
        gridShadow.setRadius(5);
        grid.setEffect(gridShadow);

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                boolean isLight = (r + c) % 2 == 0;
                Color cellBg = isLight ? Color.web("#181F2E") : Color.web("#151920");

                StackPane cellPane = new StackPane();

                // --- 1. SỬA LỖI LAYOUT BÀN CỜ ---
                // Cố định kích thước để ô không bị giãn khi thêm hình
                cellPane.setPrefSize(cellSize, cellSize);
                cellPane.setMinSize(cellSize, cellSize);
                cellPane.setMaxSize(cellSize, cellSize);
                // --------------------------------

                Button cell = new Button();
                // Cố định kích thước nút
                cell.setPrefSize(cellSize, cellSize);
                cell.setMinSize(cellSize, cellSize);
                cell.setMaxSize(cellSize, cellSize);

                cell.setBackground(new Background(new BackgroundFill(cellBg, CornerRadii.EMPTY, Insets.EMPTY)));
                cell.setBorder(new Border(new BorderStroke(Color.web("#2A2F3E"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0.5))));
                cell.setCursor(javafx.scene.Cursor.HAND);

                // Hover effect
                cell.setOnMouseEntered(e -> {
                    if (cell.getGraphic() == null && !gameState.isGameOver() && gameState.isLocalPlayersTurn()) {
                        cell.setBackground(new Background(new BackgroundFill(isLight ? Color.web("#222836") : Color.web("#1A1F28"), CornerRadii.EMPTY, Insets.EMPTY)));
                    }
                });
                cell.setOnMouseExited(e -> {
                    if (cell.getGraphic() == null || cell.getGraphic() instanceof Label) {
                        cell.setBackground(new Background(new BackgroundFill(cellBg, CornerRadii.EMPTY, Insets.EMPTY)));
                    }
                });

                final int row = r;
                final int col = c;
                cell.setOnAction(e -> controller.onLocalCellClicked(row, col));

                cells[r][c] = cell;
                cellPanes[r][c] = cellPane;

                cellPane.getChildren().add(cell);
                grid.add(cellPane, c, r);
            }
        }

        gridWithOverlay.getChildren().addAll(grid, drawLayer);
        ScrollPane scrollPane = new ScrollPane(gridWithOverlay);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: #121621; -fx-background-color: #121621;");
        scrollPane.setPannable(true);
        root.setCenter(scrollPane);

        HBox bottom = new HBox(10);
        bottom.setPadding(new Insets(10, 16, 10, 16));
        bottom.setAlignment(Pos.CENTER);
        bottom.setBackground(new Background(new BackgroundFill(Color.web("#121621"), CornerRadii.EMPTY, Insets.EMPTY)));
        bottom.setMinHeight(60);

        Button btnSurrender = createModernButton("🏳️ Xin thua", Color.web("#FF4444"), Color.web("#CC0000"));
        Button btnDraw = createModernButton("🤝 Cầu hòa", Color.web("#4A90E2"), Color.web("#357ABD"));
        Button btnLeave = createModernButton("🚪 Thoát", Color.web("#888888"), Color.web("#666666"));
        btnSurrender.setPrefWidth(140);
        btnDraw.setPrefWidth(140);
        btnLeave.setPrefWidth(140);

        Tooltip.install(btnSurrender, new Tooltip("Xin thua và kết thúc ván cờ"));
        Tooltip.install(btnDraw, new Tooltip("Gửi yêu cầu cầu hòa đến đối thủ"));
        Tooltip.install(btnLeave, new Tooltip("Thoát khỏi phòng chơi"));

        btnSurrender.setOnAction(e -> {
            // Confirm Dialog trả về boolean, truyền null cho callback
            if (showGenericDialog("Xác nhận", "Bạn có chắc muốn xin thua?", Color.web("#4A90E2"), true, null)) {
                controller.onSurrender();
            }
        });
        btnDraw.setOnAction(e -> {
            controller.onDrawRequest();
            showCustomInfoDialog("Thông báo", "Đã gửi yêu cầu cầu hòa.");
        });
        btnLeave.setOnAction(e -> {
            if (showGenericDialog("Xác nhận", "Bạn có chắc muốn thoát phòng?", Color.web("#4A90E2"), true, null)) {
                returnToHome();
            }
        });

        bottom.getChildren().addAll(btnSurrender, btnDraw, btnLeave);
        root.setBottom(bottom);

        updateTimeLabels();
        updateTurnHighlight();

        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            drawLayer.setWidth(grid.getWidth());
            drawLayer.setHeight(grid.getHeight());
        });

        return new Scene(root, screenBounds.getWidth() * 0.9, screenBounds.getHeight() * 0.9);
    }

    private VBox createPlayerCard(String playerName, char symbol, Color iconColor1, Color iconColor2) {
        HBox card = new HBox(12);
        card.setPadding(new Insets(8, 16, 8, 16));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setId("playerCard_" + symbol);
        card.setStyle("-fx-background-color: #0F1419; -fx-border-color: #2A3F5F; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");

        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(40, 40);
        iconContainer.setAlignment(Pos.CENTER);
        Circle iconBg = new Circle(20);
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, new Stop(0, iconColor1), new Stop(1, iconColor2));
        iconBg.setFill(gradient);
        DropShadow iconGlow = new DropShadow();
        iconGlow.setColor(iconColor1.interpolate(iconColor2, 0.5));
        iconGlow.setRadius(8);
        iconBg.setEffect(iconGlow);

        Label iconLabel = new Label(symbol == 'X' ? "👹" : "👽");
        iconLabel.setFont(Font.font("Arial", 20));
        iconContainer.getChildren().addAll(iconBg, iconLabel);

        VBox infoBox = new VBox(2);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label(playerName);
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        Label timeLabel = new Label("05:00");
        timeLabel.setTextFill(Color.web("#B0B0B0"));
        timeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        infoBox.getChildren().addAll(nameLabel, timeLabel);
        card.getChildren().addAll(iconContainer, infoBox);

        if (symbol == 'X') {
            avatar1 = iconBg; lblPlayer1Time = timeLabel; lblPlayer1Name = nameLabel;
        } else {
            avatar2 = iconBg; lblPlayer2Time = timeLabel; lblPlayer2Name = nameLabel;
        }
        return new VBox(card);
    }

    private VBox createCenterSection() {
        VBox center = new VBox(6);
        center.setPadding(new Insets(8, 20, 8, 20));
        center.setAlignment(Pos.CENTER);
        center.setStyle("-fx-background-color: #0F1419; -fx-border-color: #2A3F5F; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");

        HBox scoreBox = new HBox(16);
        scoreBox.setAlignment(Pos.CENTER);
        HBox player1Score = new HBox(6); player1Score.setAlignment(Pos.CENTER);
        Label crownIcon = new Label("👑"); crownIcon.setFont(Font.font("Arial", 18));
        Label score1Label = new Label("0"); score1Label.setTextFill(Color.WHITE); score1Label.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        player1Score.getChildren().addAll(crownIcon, score1Label);

        HBox player2Score = new HBox(6); player2Score.setAlignment(Pos.CENTER);
        Label skullIcon = new Label("💀"); skullIcon.setFont(Font.font("Arial", 18));
        Label score2Label = new Label("0"); score2Label.setTextFill(Color.WHITE); score2Label.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        player2Score.getChildren().addAll(skullIcon, score2Label);

        scoreBox.getChildren().addAll(player1Score, player2Score);
        center.getChildren().addAll(scoreBox);
        return center;
    }

    public void updateBoardCell(int row, int col, char symbol, boolean highlight) {
        Button cell = cells[row][col];
        StackPane cellPane = cellPanes[row][col];
        if (highlight) resetBoardHighlight();

        cellPane.getChildren().removeIf(n -> n instanceof Group || n instanceof Circle);
        Node piece = null;
        DropShadow neonGlow = new DropShadow();
        neonGlow.setRadius(15);
        neonGlow.setSpread(0.4);

        if (symbol == 'X') {
            Color xColor = Color.web("#00E5FF"); neonGlow.setColor(xColor);
            double size = cellSize * 0.5;
            Line line1 = new Line(-size/2, -size/2, size/2, size/2);
            Line line2 = new Line(-size/2, size/2, size/2, -size/2);
            for (Line line : new Line[]{line1, line2}) {
                line.setStroke(xColor); line.setStrokeWidth(4); line.setStrokeLineCap(StrokeLineCap.ROUND); line.setEffect(neonGlow);
            }
            Group xGroup = new Group(line1, line2); cellPane.getChildren().add(xGroup); piece = xGroup;
        } else if (symbol == 'O') {
            Color oColor = Color.web("#00E676"); neonGlow.setColor(oColor);
            double radius = cellSize * 0.3;
            Circle circle = new Circle(radius); circle.setFill(null); circle.setStroke(oColor); circle.setStrokeWidth(4); circle.setEffect(neonGlow);
            cellPane.getChildren().add(circle); piece = circle;
        }

        if (highlight) {
            Border highlightBorder = new Border(new BorderStroke(Color.web("#FFD700"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3)));
            cell.setBorder(highlightBorder);
            DropShadow borderGlow = new DropShadow(); borderGlow.setColor(Color.web("#FFD700")); borderGlow.setRadius(12);
            cell.setEffect(borderGlow);
            FadeTransition pulse = new FadeTransition(Duration.millis(600), cell);
            pulse.setFromValue(1.0); pulse.setToValue(0.7); pulse.setCycleCount(4); pulse.setAutoReverse(true); pulse.play();
        }
        if (piece != null) {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), piece); fadeIn.setFromValue(0); fadeIn.setToValue(1);
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), piece); scaleIn.setFromX(0.3); scaleIn.setFromY(0.3); scaleIn.setToX(1.1); scaleIn.setToY(1.1); scaleIn.setInterpolator(Interpolator.EASE_OUT);
            ScaleTransition bounce = new ScaleTransition(Duration.millis(200), piece); bounce.setToX(1.0); bounce.setToY(1.0);
            fadeIn.play(); scaleIn.play(); scaleIn.setOnFinished(e -> bounce.play());
        }
    }

    private void resetBoardHighlight() {
        int size = gameState.getBoardSize();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Button cell = cells[r][c];
                boolean isLight = (r + c) % 2 == 0;
                Color cellBg = isLight ? Color.web("#181F2E") : Color.web("#151920");
                cell.setBackground(new Background(new BackgroundFill(cellBg, CornerRadii.EMPTY, Insets.EMPTY)));
                cell.setBorder(new Border(new BorderStroke(Color.web("#2A2F3E"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0.5))));
                cell.setEffect(new DropShadow(2, Color.color(0, 0, 0, 0.2)));
            }
        }
    }

    public void updateTurnHighlight() {
        boolean p1Turn = gameState.isPlayer1Turn();
        if (p1Box != null && p1Box.getChildren().size() > 0) {
            Node card = p1Box.getChildren().get(0);
            card.setStyle("-fx-background-color: " + (p1Turn ? "#1A2335" : "#0F1419") + "; -fx-border-color: " + (p1Turn ? "#3399FF" : "#2A3F5F") + "; -fx-border-width: " + (p1Turn ? "2" : "1") + "; -fx-border-radius: 8; -fx-background-radius: 8;");
            if (avatar1 != null) avatar1.setEffect(p1Turn ? new Glow(0.6) : new DropShadow(8, Color.color(0,0,0,0.3)));
        }
        if (p2Box != null && p2Box.getChildren().size() > 0) {
            Node card = p2Box.getChildren().get(0);
            card.setStyle("-fx-background-color: " + (!p1Turn ? "#1A2335" : "#0F1419") + "; -fx-border-color: " + (!p1Turn ? "#3399FF" : "#2A3F5F") + "; -fx-border-width: " + (!p1Turn ? "2" : "1") + "; -fx-border-radius: 8; -fx-background-radius: 8;");
            if (avatar2 != null) avatar2.setEffect(!p1Turn ? new Glow(0.6) : new DropShadow(8, Color.color(0,0,0,0.3)));
        }
    }

    private void startTimer() {
        if (timeline != null) timeline.stop();
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (gameState.isGameOver()) { stopTimer(); return; }
            gameState.decrementCurrentPlayerTime(1000);
            updateTimeLabels();
            if (gameState.isCurrentPlayerOutOfTime()) { stopTimer(); controller.onTimeout(); }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.playFromStart();
    }

    public void stopTimer() { if (timeline != null) timeline.stop(); }

    private void updateTimeLabels() {
        long p1Time = gameState.getPlayer1RemainingMillis();
        long p2Time = gameState.getPlayer2RemainingMillis();
        if (lblPlayer1Time != null) {
            lblPlayer1Time.setText(formatMillis(p1Time));
            lblPlayer1Time.setTextFill(p1Time < 10000 ? Color.web("#FF4444") : (p1Time < 60000 ? Color.web("#FF8844") : Color.web("#B0B0B0")));
        }
        if (lblPlayer2Time != null) {
            lblPlayer2Time.setText(formatMillis(p2Time));
            lblPlayer2Time.setTextFill(p2Time < 10000 ? Color.web("#FF4444") : (p2Time < 60000 ? Color.web("#FF8844") : Color.web("#B0B0B0")));
        }
    }

    private String formatMillis(long millis) {
        long totalSeconds = millis / 1000;
        return String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    public void returnToHome() {
        Platform.runLater(() -> {
            stopTimer();

            // --- CẬP NHẬT MỚI: ĐÓNG TẤT CẢ DIALOG ĐANG MỞ ---
            // Lấy danh sách tất cả cửa sổ đang hiển thị của ứng dụng
            List<Window> openWindows = new ArrayList<>(Window.getWindows());

            for (Window window : openWindows) {
                // Nếu cửa sổ đó là một Stage và KHÔNG PHẢI là màn hình chính (primaryStage)
                // thì đó chắc chắn là dialog (Thắng/Thua, Thông báo,...) -> Đóng ngay
                if (window instanceof Stage && window != primaryStage) {
                    ((Stage) window).close();
                }
            }
            // ------------------------------------------------

            // Ngắt kết nối
            if (controller != null) {
                controller.disconnect();
            }

            // Reset dữ liệu game
            gameState.resetForNewGame("Player 1", "Player 2");

            // Chuyển cảnh về Home
            Scene homeScene = createHomeScene(primaryStage);
            primaryStage.setScene(homeScene);

            // Reset kích thước cửa sổ
            primaryStage.setWidth(900);
            primaryStage.setHeight(700);
            primaryStage.centerOnScreen();
        });
    }

    // --- DIALOGS ---

    public void showResultAlert(int result) {
        // Kiểm tra timeout: nếu mình hết giờ thì Code=2 (Thua), đối thủ hết giờ thì Code=1 (Thắng)
        if (gameState.isCurrentPlayerOutOfTime()) {
            boolean isPlayer1Turn = gameState.isPlayer1Turn();
            boolean amIPlayer1 = (gameState.getLocalPlayerSymbol() == 'X');
            if (isPlayer1Turn == amIPlayer1) {
                result = 2;
            } else {
                result = 1;
            }
        }

        // Code 4: Đối thủ thoát
        if (result == 4) {
            showDisconnectDialog("Thông báo", "Đối thủ đã thoát trận.\nBạn giành chiến thắng!");
            return;
        }

        String title = "";
        String message = "";
        Color borderColor = Color.TRANSPARENT;

        if (result == 1) {
            title = "CHIẾN THẮNG";
            message = "Bạn đã chiến thắng!";
            borderColor = Color.web("#FFD700");
        } else if (result == 2) {
            title = "THẤT BẠI";
            message = "Bạn đã thua cuộc!";
            borderColor = Color.web("#FF4444");
        } else if (result == 3) {
            title = "HÒA";
            message = "Ván đấu hòa!";
            borderColor = Color.web("#50C878");
        }

        showEndGameDialog(title, message, borderColor);
    }

    private void showDisconnectDialog(String title, String message) {
        Platform.runLater(() -> {
            closeAllDialogs();

            Stage d = new Stage();
            d.initStyle(StageStyle.UNDECORATED);
            d.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            if (primaryStage != null) d.initOwner(primaryStage);

            VBox v = new VBox(20);
            v.setPadding(new Insets(24));
            v.setAlignment(Pos.CENTER);
            v.setStyle("-fx-background-color: #1A1F2E; -fx-border-color: #FFD700; -fx-border-width: 2; -fx-border-radius: 12; -fx-background-radius: 12;");

            Label t = new Label(title); t.setTextFill(Color.WHITE); t.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            Label m = new Label(message); m.setTextFill(Color.LIGHTGRAY); m.setWrapText(true); m.setMaxWidth(300); m.setAlignment(Pos.CENTER);

            // Nút về trang chủ
            Button btnHome = createModernButton("Về trang chủ", Color.web("#4A90E2"), Color.web("#357ABD"));
            btnHome.setPrefWidth(140);
            btnHome.setOnAction(e -> { d.close(); returnToHome(); });
            d.setOnCloseRequest(e -> returnToHome());

            v.getChildren().addAll(t, m, btnHome);
            d.setScene(new Scene(v, Color.TRANSPARENT));
            d.show();
        });
    }

    private void showEndGameDialog(String title, String message, Color borderColor) {
        Platform.runLater(() -> {
            closeAllDialogs();

            Stage d = new Stage();
            activeDialog = d;

            d.initStyle(StageStyle.UNDECORATED);
            d.initModality(javafx.stage.Modality.NONE);
            if (primaryStage != null) d.initOwner(primaryStage);

            VBox v = new VBox(20);
            v.setPadding(new Insets(24));
            v.setAlignment(Pos.CENTER);
            v.setStyle("-fx-background-color: #1A1F2E; -fx-border-color: " + toHex(borderColor) + "; -fx-border-width: 2; -fx-border-radius: 12; -fx-background-radius: 12;");

            Label t = new Label(title); t.setTextFill(Color.WHITE); t.setFont(Font.font("Arial", FontWeight.BOLD, 22));
            Label m = new Label(message); m.setTextFill(Color.LIGHTGRAY); m.setWrapText(true); m.setMaxWidth(300); m.setAlignment(Pos.CENTER);

            HBox btns = new HBox(15);
            btns.setAlignment(Pos.CENTER);

            Button btnRematch = createModernButton("🔄 Chơi lại", Color.web("#4A90E2"), Color.web("#357ABD"));
            btnRematch.setPrefWidth(120);
            btnRematch.setOnAction(e -> {
                d.close();
                controller.onPlayAgainRequest();
                showLoadingDialog("Đang chờ...", "Đang gửi yêu cầu chơi lại...");
            });

            Button btnExit = createModernButton("Thoát", Color.web("#FF4444"), Color.web("#CC0000"));
            btnExit.setPrefWidth(120);

            // --- NÚT THOÁT VỀ TRANG CHỦ ---
            btnExit.setOnAction(e -> {
                d.close();
                returnToHome();
            });
            // -----------------------------

            btns.getChildren().addAll(btnRematch, btnExit);
            v.getChildren().addAll(t, m, btns);

            d.setScene(new Scene(v, Color.TRANSPARENT));
            d.show();
        });
    }

    public void showPlayAgainConfirmDialog(Runnable onAccept, Runnable onReject) {
        Platform.runLater(() -> {
            closeAllDialogs();
            boolean agreed = showGenericDialog("Yêu cầu chơi lại", "Đối thủ muốn chơi ván mới. Bạn có đồng ý?", Color.web("#50C878"), true, null);
            if (agreed) {
                onAccept.run();
                showLoadingDialog("Đang thiết lập...", "Đang chuẩn bị ván mới...");
            } else {
                onReject.run();
            }
        });
    }

    // --- CÁC PHƯƠNG THỨC DIALOG ---

    public void showCustomErrorDialog(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // --- 2. THÊM PHƯƠNG THỨC TIN NHẮN CÓ CALLBACK ---
    public void showCustomMessageDialog(String message, Runnable onOk) {
        Platform.runLater(() -> showGenericDialog("Thông báo", message, Color.web("#50C878"), false, onOk));
    }

    public void showCustomMessageDialog(String message) {
        Platform.runLater(() -> showGenericDialog("Thông báo", message, Color.web("#50C878"), false, null));
    }

    public void showCustomDrawRequestDialog(Runnable onAccepted, Runnable onRejected) {
        Platform.runLater(() -> {
            if (showGenericDialog("Cầu hòa", "Đối thủ muốn cầu hòa. Bạn đồng ý không?", Color.web("#4A90E2"), true, null)) onAccepted.run(); else onRejected.run();
        });
    }

    public void showCustomInfoDialog(String title, String message) {
        Platform.runLater(() -> showGenericDialog(title, message, Color.web("#50C878"), false, null));
    }

    public void showCustomWarningDialog(String title, String message) {
        Platform.runLater(() -> showGenericDialog(title, message, Color.web("#FF8844"), false, null));
    }

    private boolean showCustomConfirmDialog(String title, String message) {
        return showGenericDialog(title, message, Color.web("#4A90E2"), true, null);
    }

    // --- 3. CẬP NHẬT showGenericDialog ĐỂ NHẬN CALLBACK ---
    private boolean showGenericDialog(String title, String message, Color color, boolean isConfirm, Runnable onOk) {
        Stage d = new Stage();
        d.initStyle(StageStyle.UNDECORATED);
        d.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        if (primaryStage != null) d.initOwner(primaryStage);

        VBox v = new VBox(20); v.setPadding(new Insets(24)); v.setAlignment(Pos.CENTER);
        v.setStyle("-fx-background-color: #1A1F2E; -fx-border-color: " + toHex(color) + "; -fx-border-width: 2; -fx-border-radius: 12; -fx-background-radius: 12;");
        Label t = new Label(title); t.setTextFill(Color.WHITE); t.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        Label m = new Label(message); m.setTextFill(Color.LIGHTGRAY); m.setWrapText(true); m.setMaxWidth(300); m.setAlignment(Pos.CENTER);

        HBox btns = new HBox(10); btns.setAlignment(Pos.CENTER);
        Button ok = createModernButton(isConfirm ? "Đồng ý" : "OK", color, color.darker()); ok.setPrefWidth(100);

        final boolean[] res = {false};

        // --- XỬ LÝ SỰ KIỆN OK ---
        ok.setOnAction(e -> {
            res[0] = true;
            d.close();
            if (onOk != null) onOk.run();
        });
        // ------------------------

        btns.getChildren().add(ok);
        if (isConfirm) {
            Button cancel = createModernButton("Hủy", Color.GRAY, Color.DARKGRAY); cancel.setPrefWidth(100);
            cancel.setOnAction(e -> d.close());
            btns.getChildren().add(cancel);
        }
        v.getChildren().addAll(t, m, btns);
        d.setScene(new Scene(v, Color.TRANSPARENT));
        d.showAndWait();
        return res[0];
    }

    public void showLoadingDialog(String title, String message) {
        Platform.runLater(() -> {
            // Nếu đang có dialog và đang hiển thị -> Chỉ cần return hoặc đóng cái cũ
            if (loadingDialogStage != null && loadingDialogStage.isShowing()) {
                loadingDialogStage.close();
            }

            loadingDialogStage = new Stage();
            loadingDialogStage.initStyle(StageStyle.UNDECORATED);
            if (primaryStage != null) loadingDialogStage.initOwner(primaryStage);

            VBox box = new VBox(20);
            box.setAlignment(Pos.CENTER);
            box.setPadding(new Insets(30));
            box.setStyle("-fx-background-color: #1A1F2E; -fx-border-color: #4A90E2; -fx-border-width: 2; -fx-border-radius: 12; -fx-background-radius: 12;");

            Circle spinner = new Circle(20);
            spinner.setFill(null);
            spinner.setStroke(Color.web("#4A90E2"));
            spinner.setStrokeWidth(3);

            RotateTransition rt = new RotateTransition(Duration.seconds(1), spinner);
            rt.setByAngle(360);
            rt.setCycleCount(-1);
            rt.play();

            Label t = new Label(title);
            t.setTextFill(Color.WHITE);
            t.setFont(Font.font(16));

            Label m = new Label(message);
            m.setTextFill(Color.GRAY);

            box.getChildren().addAll(spinner, t, m);
            loadingDialogStage.setScene(new Scene(box, Color.TRANSPARENT));
            loadingDialogStage.show();
        });
    }

    public void hideLoadingDialog() {
        Platform.runLater(() -> { if (loadingDialogStage != null) { loadingDialogStage.close(); loadingDialogStage = null; }});
    }

    private Button createModernButton(String text, Color color1, Color color2) {
        Button button = new Button(text);
        button.setTextFill(Color.WHITE);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        button.setPrefHeight(38);
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, new Stop(0, color1), new Stop(1, color2));
        button.setBackground(new Background(new BackgroundFill(gradient, new CornerRadii(6), Insets.EMPTY)));
        button.setBorder(new Border(new BorderStroke(Color.web("#FFFFFF", 0.2), BorderStrokeStyle.SOLID, new CornerRadii(6), new BorderWidths(1))));
        DropShadow glow = new DropShadow(); glow.setColor(color1); glow.setRadius(6); button.setEffect(glow);
        button.setOnMouseEntered(e -> {
            button.setBackground(new Background(new BackgroundFill(new LinearGradient(0,0,0,1,true,CycleMethod.NO_CYCLE, new Stop(0, color1.deriveColor(0,1,1.1,1)), new Stop(1, color2.deriveColor(0,1,1.1,1))), new CornerRadii(6), Insets.EMPTY)));
            glow.setRadius(10);
        });
        button.setOnMouseExited(e -> {
            button.setBackground(new Background(new BackgroundFill(gradient, new CornerRadii(6), Insets.EMPTY)));
            glow.setRadius(6);
        });
        return button;
    }

    private String toHex(Color c) {
        return String.format("#%02X%02X%02X", (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255));
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        // Khi tắt hẳn ứng dụng bằng nút X, cũng phải ngắt kết nối
        if (controller != null) {
            controller.disconnect();
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}