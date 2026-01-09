package client;

import client.controller.ClientController;
import client.model.GameState;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainClientFX extends Application {

    private GameState gameState;
    private ClientController controller;
    private Stage primaryStage;

    // UI Components
    private Label lblPlayer1Time, lblPlayer2Time;
    private Label lblPlayer1Name, lblPlayer2Name;
    private Label lblScore1, lblScore2;
    private Circle avatar1, avatar2;

    // Grid
    private Button[][] cells;
    private StackPane[][] cellPanes;
    private Canvas drawLayer;

    // Logic visuals
    private StackPane lastMoveHighlight; // Bracket cursor
    private Timeline timeline;

    // Constants for Design
    private static final Color BG_COLOR = Color.web("#0d1117");
    private static final Color BOARD_BG_EVEN = Color.web("#161b22");
    private static final Color BOARD_BG_ODD = Color.web("#0d1117");
    private static final Color NEON_BLUE = Color.web("#00f2ff");
    private static final Color NEON_GREEN = Color.web("#00ff88");
    private static final Color NEON_RED = Color.web("#ff0055");
    private static final String FONT_FAMILY = "Segoe UI";

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.gameState = new GameState();
        this.controller = new ClientController(gameState, this);

        Scene homeScene = createHomeScene();
        stage.setTitle("Caro FX - Neon Battle");
        stage.setScene(homeScene);
        stage.show();
    }

    public void switchToGameScene() {
        if (primaryStage != null) {
            Scene gameScene = createGameScene();
            primaryStage.setScene(gameScene);
            // Cập nhật tên người chơi từ model khi vào game
            updatePlayerInfo();
            startTimer();
        }
    }

    // ==========================================
    // HOME SCENE
    // ==========================================
    private Scene createHomeScene() {
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setBackground(new Background(new BackgroundFill(BG_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));

        VBox mainContainer = new VBox(30);
        mainContainer.setPadding(new Insets(40));
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setMaxWidth(500);

        // Styling Container
        mainContainer.setStyle(
                "-fx-background-color: #161b22; -fx-background-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 20, 0, 0, 10);");

        // Title
        Label title = new Label("CARO NEON");
        title.setTextFill(NEON_BLUE);
        title.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 48));
        title.setEffect(new DropShadow(BlurType.GAUSSIAN, NEON_BLUE, 20, 0.5, 0, 0));

        // Input
        TextField txtName = new TextField();
        txtName.setPromptText("Enter your codename...");
        txtName.setStyle(
                "-fx-background-color: #0d1117; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 15; -fx-border-color: #30363d; -fx-border-radius: 5; -fx-background-radius: 5;");

        // Play Button
        Button btnPlay = new Button("START MISSION");
        btnPlay.setStyle(
                "-fx-background-color: linear-gradient(to right, #238636, #2ea043); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 15 40; -fx-cursor: hand; -fx-background-radius: 5;");
        btnPlay.setOnMouseEntered(e -> btnPlay.setStyle(
                "-fx-background-color: linear-gradient(to right, #2ea043, #3fb950); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 15 40; -fx-cursor: hand; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(46, 160, 67, 0.6), 15, 0, 0, 0);"));
        btnPlay.setOnMouseExited(e -> btnPlay.setStyle(
                "-fx-background-color: linear-gradient(to right, #238636, #2ea043); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 15 40; -fx-cursor: hand; -fx-background-radius: 5;"));

        btnPlay.setOnAction(e -> {
            String name = txtName.getText();
            if (name == null || name.trim().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Please enter your name.").showAndWait();
                return;
            }
            controller.onPlayNow(name.trim());
        });

        mainContainer.getChildren().addAll(title, txtName, btnPlay);
        root.getChildren().add(mainContainer);

        return new Scene(root, 900, 750);
    }

    // ==========================================
    // GAME SCENE
    // ==========================================
    private Scene createGameScene() {
        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(BG_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));

        // --- TOP HUD ---
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15, 30, 15, 30));
        topBar.setAlignment(Pos.CENTER);
        topBar.setStyle("-fx-background-color: #161b22; -fx-border-color: #30363d; -fx-border-width: 0 0 1 0;");

        // Player 1 (Left - Red/Demon theme logic, visually customize)
        HBox p1Box = createPlayerBox(true);
        // Player 2 (Right - Green/Alien theme logic)
        HBox p2Box = createPlayerBox(false);
        // Score (Center)
        HBox scoreBox = createScoreBox();

        Region spacerL = new Region();
        HBox.setHgrow(spacerL, Priority.ALWAYS);
        Region spacerR = new Region();
        HBox.setHgrow(spacerR, Priority.ALWAYS);

        topBar.getChildren().addAll(p1Box, spacerL, scoreBox, spacerR, p2Box);
        root.setTop(topBar);

        // --- CENTER GRID ---
        int size = gameState.getBoardSize();
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(1);
        grid.setVgap(1);
        grid.setBackground(new Background(new BackgroundFill(Color.web("#30363d"), CornerRadii.EMPTY, Insets.EMPTY))); // Border
                                                                                                                       // color

        cells = new Button[size][size];
        cellPanes = new StackPane[size][size];

        // Tính toán size ô
        double cellSize = 38;

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                StackPane stack = new StackPane();
                stack.setPrefSize(cellSize, cellSize);

                // Checkerboard effect
                Color cellColor = ((r + c) % 2 == 0) ? BOARD_BG_EVEN : BOARD_BG_ODD;
                stack.setBackground(new Background(new BackgroundFill(cellColor, CornerRadii.EMPTY, Insets.EMPTY)));

                Button btn = new Button();
                btn.setPrefSize(cellSize, cellSize);
                btn.setStyle("-fx-background-color: transparent;");
                btn.setCursor(javafx.scene.Cursor.HAND);

                // Hover effect (nhẹ)
                btn.setOnMouseEntered(event -> {
                    if (btn.getGraphic() == null)
                        stack.setStyle("-fx-background-color: #21262d;");
                });
                btn.setOnMouseExited(event -> {
                    if (btn.getGraphic() == null)
                        stack.setBackground(
                                new Background(new BackgroundFill(cellColor, CornerRadii.EMPTY, Insets.EMPTY)));
                });

                final int row = r;
                final int col = c;
                btn.setOnAction(event -> controller.onLocalCellClicked(row, col));

                cells[r][c] = btn;
                cellPanes[r][c] = stack;
                stack.getChildren().add(btn);
                grid.add(stack, c, r);
            }
        }

        // Overlay for drawing winning lines
        drawLayer = new Canvas(size * cellSize + (size - 1), size * cellSize + (size - 1));
        drawLayer.setMouseTransparent(true);

        StackPane centerStack = new StackPane(grid, drawLayer);
        centerStack.setPadding(new Insets(20));
        centerStack.setAlignment(Pos.CENTER);

        // Glow effect dưới bàn cờ
        DropShadow boardGlow = new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.5), 30, 0.0, 0, 10);
        centerStack.setEffect(boardGlow);

        root.setCenter(centerStack);

        // --- BOTTOM CONTROLS ---
        HBox bottomBar = new HBox(15);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setPadding(new Insets(20));
        bottomBar.setStyle("-fx-background-color: #0d1117;");

        Button btnSurrender = createGlassButton("Xin thua", "#da3633");
        Button btnDraw = createGlassButton("Cầu hòa", "#8b949e");
        Button btnExit = createGlassButton("Thoát", "#30363d");

        btnSurrender.setOnAction(_ -> {
            if (confirmAction("Bạn muốn xin thua?"))
                controller.onSurrender();
        });
        btnDraw.setOnAction(_ -> {
            controller.onDrawRequest();
            showAlert("Đã gửi yêu cầu hòa.");
        });
        btnExit.setOnAction(_ -> {
            if (confirmAction("Thoát game?")) {
                stopTimer();
                primaryStage.setScene(createHomeScene());
            }
        });

        bottomBar.getChildren().addAll(btnSurrender, btnDraw, btnExit);
        root.setBottom(bottomBar);

        updateTurnHighlight();
        return new Scene(root, 1000, 850);
    }

    private HBox createPlayerBox(boolean isLeft) {
        HBox box = new HBox(12);
        box.setAlignment(isLeft ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);

        // Components
        Circle avatar = new Circle(22);
        avatar.setFill(Color.TRANSPARENT);
        avatar.setStroke(isLeft ? NEON_RED : NEON_GREEN);
        avatar.setStrokeWidth(2);
        // Placeholder Icon inside avatar
        Label icon = new Label(isLeft ? "😈" : "👽");
        icon.setFont(Font.font(20));
        StackPane avatarPane = new StackPane(avatar, icon);

        Label name = new Label(isLeft ? "Player 1" : "Player 2");
        name.setTextFill(Color.WHITE);
        name.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));

        Label time = new Label("05:00");
        time.setTextFill(Color.web("#8b949e"));
        time.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        time.setPadding(new Insets(4, 10, 4, 10));
        time.setStyle("-fx-border-color: #30363d; -fx-border-radius: 4; -fx-border-width: 1;");

        if (isLeft) {
            box.getChildren().addAll(avatarPane, name, time);
            this.avatar1 = avatar;
            this.lblPlayer1Name = name;
            this.lblPlayer1Time = time;
        } else {
            box.getChildren().addAll(time, name, avatarPane);
            this.avatar2 = avatar;
            this.lblPlayer2Name = name;
            this.lblPlayer2Time = time;
        }
        return box;
    }

    private HBox createScoreBox() {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER);
        box.setStyle(
                "-fx-background-color: #0d1117; -fx-background-radius: 20; -fx-padding: 5 20; -fx-border-color: #30363d; -fx-border-radius: 20;");

        lblScore1 = new Label("0");
        lblScore1.setTextFill(NEON_BLUE);
        lblScore1.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 20));

        Label iconCrown = new Label("👑");
        iconCrown.setTextFill(Color.GOLD);

        Label dot = new Label("•");
        dot.setTextFill(Color.GRAY);

        Label iconSkull = new Label("💀");
        iconSkull.setTextFill(Color.GRAY);

        lblScore2 = new Label("0");
        lblScore2.setTextFill(NEON_RED);
        lblScore2.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 20));

        box.getChildren().addAll(iconCrown, lblScore1, dot, lblScore2, iconSkull);
        return box;
    }

    private Button createGlassButton(String text, String colorHex) {
        Button btn = new Button(text);
        btn.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 20;",
                colorHex));
        btn.setPrefWidth(120);
        return btn;
    }

    // ==========================================
    // GAME LOGIC UI
    // ==========================================

    // Cập nhật tên người chơi từ GameState (tránh warning unused fields)
    public void updatePlayerInfo() {
        if (lblPlayer1Name != null)
            lblPlayer1Name.setText(gameState.getPlayer1Name());
        if (lblPlayer2Name != null)
            lblPlayer2Name.setText(gameState.getPlayer2Name());
    }

    public void updateBoardCell(int row, int col, char symbol, boolean highlight) {
        Button cell = cells[row][col];
        StackPane pane = cellPanes[row][col];

        // Remove old content
        pane.getChildren().removeIf(n -> n != cell && n != lastMoveHighlight);

        // 1. Draw Symbol (X or O)
        Node graphic = null;
        if (symbol == 'X') {
            graphic = createNeonX();
        } else if (symbol == 'O') {
            graphic = createNeonO();
        }

        if (graphic != null) {
            pane.getChildren().add(graphic);
            // Animation pop
            ScaleTransition st = new ScaleTransition(Duration.millis(300), graphic);
            st.setFromX(0.1);
            st.setFromY(0.1);
            st.setToX(1);
            st.setToY(1);
            st.play();
        }

        // 2. Draw Last Move Highlight (Bracket Cursor)
        if (highlight) {
            highlightLastMove(row, col);
        }
    }

    private Node createNeonX() {
        Group group = new Group();
        double size = 20;

        Line l1 = new Line(-size, -size, size, size);
        Line l2 = new Line(-size, size, size, -size);

        for (Line l : new Line[] { l1, l2 }) {
            l.setStroke(NEON_BLUE);
            l.setStrokeWidth(4);
            l.setStrokeLineCap(StrokeLineCap.ROUND);
            l.setEffect(new DropShadow(BlurType.THREE_PASS_BOX, NEON_BLUE, 15, 0.6, 0, 0));
        }

        group.getChildren().addAll(l1, l2);
        return group;
    }

    private Node createNeonO() {
        Circle c = new Circle(16);
        c.setFill(Color.TRANSPARENT);
        c.setStroke(NEON_GREEN);
        c.setStrokeWidth(4);
        c.setEffect(new DropShadow(BlurType.THREE_PASS_BOX, NEON_GREEN, 15, 0.6, 0, 0));
        return c;
    }

    private void highlightLastMove(int row, int col) {
        // Remove previous highlight
        if (lastMoveHighlight != null) {
            ((StackPane) lastMoveHighlight.getParent()).getChildren().remove(lastMoveHighlight);
        }

        // Create new Bracket Cursor (4 góc)
        StackPane cursor = new StackPane();
        cursor.setMouseTransparent(true);
        cursor.setPrefSize(38, 38);

        double len = 10;
        double gap = 38;
        Color color = NEON_GREEN; // Cursor color
        double w = 2;

        // Top-Left
        Path tl = new Path(new MoveTo(0, len), new LineTo(0, 0), new LineTo(len, 0));
        // Top-Right
        Path tr = new Path(new MoveTo(gap - len, 0), new LineTo(gap, 0), new LineTo(gap, len));
        // Bottom-Left
        Path bl = new Path(new MoveTo(0, gap - len), new LineTo(0, gap), new LineTo(len, gap));
        // Bottom-Right
        Path br = new Path(new MoveTo(gap - len, gap), new LineTo(gap, gap), new LineTo(gap, gap - len));

        for (Path p : new Path[] { tl, tr, bl, br }) {
            p.setStroke(color);
            p.setStrokeWidth(w);
            p.setEffect(new DropShadow(BlurType.GAUSSIAN, color, 5, 0.5, 0, 0));
            cursor.getChildren().add(p);
            StackPane.setAlignment(p, Pos.TOP_LEFT); // Paths are relative coordinates, this is fine
        }

        cellPanes[row][col].getChildren().add(cursor);
        lastMoveHighlight = cursor;
    }

    public void updateTurnHighlight() {
        boolean isP1 = gameState.isPlayer1Turn();

        // Dim opacity of inactive player
        if (avatar1 != null && avatar2 != null) {
            avatar1.setOpacity(isP1 ? 1.0 : 0.4);
            avatar2.setOpacity(!isP1 ? 1.0 : 0.4);

            // Glow effect for active player name
            lblPlayer1Name.setEffect(isP1 ? new DropShadow(BlurType.GAUSSIAN, NEON_RED, 10, 0.5, 0, 0) : null);
            lblPlayer2Name.setEffect(!isP1 ? new DropShadow(BlurType.GAUSSIAN, NEON_GREEN, 10, 0.5, 0, 0) : null);
        }
    }

    private void startTimer() {
        if (timeline != null)
            timeline.stop();
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (gameState.isGameOver()) {
                stopTimer();
                return;
            }
            gameState.decrementCurrentPlayerTime(1000);
            updateTimeLabels();
            if (gameState.isCurrentPlayerOutOfTime()) {
                stopTimer();
                controller.onTimeout();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void stopTimer() {
        if (timeline != null)
            timeline.stop();
    }

    private void updateTimeLabels() {
        if (lblPlayer1Time != null)
            lblPlayer1Time.setText(formatMillis(gameState.getPlayer1RemainingMillis()));
        if (lblPlayer2Time != null)
            lblPlayer2Time.setText(formatMillis(gameState.getPlayer2RemainingMillis()));
    }

    private String formatMillis(long millis) {
        long s = millis / 1000;
        return String.format("%02d:%02d", s / 60, s % 60);
    }

    public void showResultAlert(int resultCode) {
        String title, message;
        switch (resultCode) {
            case 1: // Thắng
                title = "🎉 Chiến thắng!";
                message = "Chúc mừng! Bạn đã thắng!";
                break;
            case 2: // Thua
                title = "😢 Thua cuộc";
                message = "Bạn đã thua. Cố gắng lần sau!";
                break;
            case 3: // Hòa
                title = "🤝 Hòa";
                message = "Trận đấu kết thúc hòa.";
                break;
            case 4: // Đối thủ thoát
                title = "⚠️ Đối thủ rời phòng";
                message = "Đối thủ đã thoát khỏi trận đấu.";
                break;
            default:
                title = "Kết thúc";
                message = "Trận đấu đã kết thúc.";
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Helper Dialogs
    private boolean confirmAction(String msg) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.initOwner(primaryStage);
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    public void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.setHeaderText(null);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}