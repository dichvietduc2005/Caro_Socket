package client;

import client.controller.ClientController;
import client.model.GameState;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.canvas.Canvas;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Interpolator;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.KeyValue;

public class MainClientFX extends Application {

    private GameState gameState;

    private Label lblPlayer1Time;
    private Label lblPlayer2Time;

    private Circle avatar1;
    private Circle avatar2;

    // Lưu progressFill để update width
    private javafx.scene.shape.Rectangle progressFill1;
    private javafx.scene.shape.Rectangle progressFill2;
    private VBox p1Box;
    private VBox p2Box;
    private Button[][] cells;
    private StackPane[][] cellPanes; // Để chứa Button + Shape (X/O)

    private Canvas drawLayer; // Canvas để vẽ đường chiến thắng
    private double cellSize;
    private Stage primaryStage;
    private ClientController controller;

    private Timeline timeline;
    private static final double INITIAL_TIME_SECONDS = 5 * 60; // 5 phút

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.gameState = new GameState();
        this.controller = new ClientController(gameState, this);

        Scene homeScene = createHomeScene(stage);
        stage.setTitle("Caro FX - Networking Mode");
        stage.setScene(homeScene);
        stage.show();
    }

    public void switchToGameScene() {
        if (primaryStage != null) {
            Scene gameScene = createGameScene(primaryStage);
            primaryStage.setScene(gameScene);
            startTimer();
        }
    }

    private Scene createHomeScene(Stage stage) {
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setBackground(new Background(new BackgroundFill(Color.web("#0A0E1A"), CornerRadii.EMPTY, Insets.EMPTY)));

        // Main container
        VBox mainContainer = new VBox(40);
        mainContainer.setPadding(new Insets(60, 80, 60, 80));
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setMaxWidth(600);

        // Title với gradient effect
        Label title = new Label("CARO");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 72));
        title.setAlignment(Pos.CENTER);

        Label subtitle = new Label("Trò chơi cờ caro 5 nước thắng");
        subtitle.setTextFill(Color.web("#B0B0B0"));
        subtitle.setFont(Font.font("Arial", 16));
        subtitle.setAlignment(Pos.CENTER);

        VBox titleBox = new VBox(12);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.getChildren().addAll(title, subtitle);

        // Separator
        javafx.scene.shape.Line separator = new javafx.scene.shape.Line(0, 0, 400, 0);
        separator.setStroke(Color.web("#4A90E2"));
        separator.setStrokeWidth(2);

        // Input section
        VBox inputSection = new VBox(16);
        inputSection.setAlignment(Pos.CENTER);

        Label lblName = new Label("Nhập tên của bạn");
        lblName.setTextFill(Color.WHITE);
        lblName.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        TextField txtName = new TextField();
        txtName.setPromptText("Nhập tên người chơi...");
        txtName.setStyle(
                "-fx-font-size: 16; " +
                        "-fx-padding: 16; " +
                        "-fx-background-color: #1A1F2E; " +
                        "-fx-text-fill: white; " +
                        "-fx-prompt-text-fill: #888888; " +
                        "-fx-border-color: #4A90E2; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8;");
        txtName.setPrefHeight(50);
        txtName.setPrefWidth(300);

        // Focus effect
        txtName.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                // Focused
                txtName.setStyle(
                        "-fx-font-size: 16; " +
                                "-fx-padding: 16; " +
                                "-fx-background-color: #1A1F2E; " +
                                "-fx-text-fill: white; " +
                                "-fx-prompt-text-fill: #888888; " +
                                "-fx-border-color: #6AB5FF; " +
                                "-fx-border-width: 2; " +
                                "-fx-border-radius: 8; " +
                                "-fx-background-radius: 8;");
            } else {
                // Unfocused
                txtName.setStyle(
                        "-fx-font-size: 16; " +
                                "-fx-padding: 16; " +
                                "-fx-background-color: #1A1F2E; " +
                                "-fx-text-fill: white; " +
                                "-fx-prompt-text-fill: #888888; " +
                                "-fx-border-color: #4A90E2; " +
                                "-fx-border-width: 2; " +
                                "-fx-border-radius: 8; " +
                                "-fx-background-radius: 8;");
            }
        });

        inputSection.getChildren().addAll(lblName, txtName);

        // Play button
        Button btnPlay = new Button("CHƠI NGAY");
        btnPlay.setStyle(
                "-fx-font-size: 18; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 16 60 16 60; " +
                        "-fx-background-color: #2D89EF; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;");
        btnPlay.setPrefHeight(50);

        // Hover effect
        btnPlay.setOnMouseEntered(e -> btnPlay.setStyle(
                "-fx-font-size: 18; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 16 60 16 60; " +
                        "-fx-background-color: #4A9FFF; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"));

        btnPlay.setOnMouseExited(e -> btnPlay.setStyle(
                "-fx-font-size: 18; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 16 60 16 60; " +
                        "-fx-background-color: #2D89EF; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"));

        btnPlay.setOnAction(e -> {
            String name = txtName.getText();
            if (name == null || name.trim().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Vui lòng nhập tên người chơi.").showAndWait();
                return;
            }
            controller.onPlayNow(name.trim());
            // Đợi Server gửi StartPacket mới chuyển sang màn hình chơi
        });

        // Footer info
        Label footerInfo = new Label("Chơi ngay để thử thách bản thân");
        footerInfo.setTextFill(Color.web("#888888"));
        footerInfo.setFont(Font.font("Arial", 14));

        mainContainer.getChildren().addAll(
                titleBox,
                separator,
                inputSection,
                btnPlay,
                footerInfo);

        root.getChildren().add(mainContainer);
        return new Scene(root, 900, 900);
    }

    private Scene createGameScene(Stage stage) {
        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(Color.web("#121621"), CornerRadii.EMPTY, Insets.EMPTY)));

        HBox top = new HBox(12);
        top.setPadding(new Insets(16));
        top.setAlignment(Pos.CENTER);
        top.setBackground(new Background(new BackgroundFill(Color.web("#121621"), CornerRadii.EMPTY, Insets.EMPTY)));

        // Player 1 Card với Avatar
        p1Box = createPlayerCard(
                gameState.getPlayer1Name(),
                'X',
                Color.web("#4A90E2"),
                Color.web("#357ABD"));
        p1Box.setId("player1Card");

        // Player 2 Card với Avatar
        p2Box = createPlayerCard(
                gameState.getPlayer2Name(),
                'O',
                Color.web("#50C878"),
                Color.web("#3FA568"));
        p2Box.setId("player2Card");

        HBox.setHgrow(p1Box, Priority.ALWAYS);
        HBox.setHgrow(p2Box, Priority.ALWAYS);
        p1Box.setMaxWidth(Double.MAX_VALUE);
        p2Box.setMaxWidth(Double.MAX_VALUE);

        top.getChildren().addAll(p1Box, p2Box);
        root.setTop(top);

        int size = gameState.getBoardSize();
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(16));
        grid.setHgap(2);
        grid.setVgap(2);
        grid.setAlignment(Pos.CENTER);
        grid.setBackground(new Background(new BackgroundFill(Color.web("#181F2E"), CornerRadii.EMPTY, Insets.EMPTY)));

        cells = new Button[size][size];
        cellPanes = new StackPane[size][size];
        // Tính toán kích thước ô để vừa với cửa sổ
        this.cellSize = Math.min(35, (900 - 100) / size - 2);

        // Tạo Canvas để vẽ đường chiến thắng
        // Size sẽ được set sau khi grid được layout
        this.drawLayer = new Canvas(800, 800); // Kích thước tạm
        drawLayer.setMouseTransparent(true);

        // StackPane để chứa grid + canvas overlay
        StackPane gridWithOverlay = new StackPane();
        gridWithOverlay.setAlignment(Pos.CENTER);

        // Tạo đổ bóng nhẹ cho grid
        DropShadow gridShadow = new DropShadow();
        gridShadow.setColor(Color.color(0, 0, 0, 0.3));
        gridShadow.setRadius(5);
        grid.setEffect(gridShadow);

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                // Màu nền xen kẽ (checkerboard pattern)
                boolean isLight = (r + c) % 2 == 0;
                Color cellBg = isLight ? Color.web("#181F2E") : Color.web("#151920");

                StackPane cellPane = new StackPane();
                cellPane.setPrefSize(cellSize, cellSize);
                cellPane.setMinSize(cellSize, cellSize);
                cellPane.setMaxSize(cellSize, cellSize);

                Button cell = new Button();
                cell.setPrefSize(cellSize, cellSize);
                cell.setMinSize(cellSize, cellSize);
                cell.setMaxSize(cellSize, cellSize);
                cell.setBackground(new Background(new BackgroundFill(cellBg, CornerRadii.EMPTY, Insets.EMPTY)));

                // Border với hiệu ứng nổi
                Border cellBorder = new Border(new BorderStroke(
                        Color.web("#2A2F3E"),
                        BorderStrokeStyle.SOLID,
                        CornerRadii.EMPTY,
                        new BorderWidths(0.5)));
                cell.setBorder(cellBorder);

                // Đổ bóng nhẹ cho từng ô
                DropShadow cellShadow = new DropShadow();
                cellShadow.setColor(Color.color(0, 0, 0, 0.2));
                cellShadow.setRadius(2);
                cell.setEffect(cellShadow);

                cell.setCursor(javafx.scene.Cursor.HAND);

                // Hover effects với hiệu ứng nổi mạnh hơn
                cell.setOnMouseEntered(e -> {
                    if (cell.getGraphic() == null) {
                        Color hoverBg = isLight ? Color.web("#222836") : Color.web("#1A1F28");
                        cell.setBackground(
                                new Background(new BackgroundFill(hoverBg, CornerRadii.EMPTY, Insets.EMPTY)));
                        DropShadow hoverShadow = new DropShadow();
                        hoverShadow.setColor(Color.web("#4A90E2"));
                        hoverShadow.setRadius(4);
                        hoverShadow.setSpread(0.3);
                        cell.setEffect(hoverShadow);
                    }
                });
                cell.setOnMouseExited(e -> {
                    if (cell.getGraphic() == null) {
                        cell.setBackground(new Background(new BackgroundFill(cellBg, CornerRadii.EMPTY, Insets.EMPTY)));
                        cell.setEffect(cellShadow);
                    }
                });

                final int row = r;
                final int col = c;
                cell.setOnAction(e -> controller.onLocalCellClicked(row, col));

                cells[r][c] = cell;
                cellPanes[r][c] = cellPane;
                cellPane.getChildren().add(cell);
                grid.add(cellPane, c, r);
                GridPane.setHalignment(cellPane, HPos.CENTER);
            }
        }

        // Thêm grid vào overlay container
        gridWithOverlay.getChildren().addAll(grid, drawLayer);
        root.setCenter(gridWithOverlay);

        // Bottom panel với các nút chức năng
        HBox bottom = new HBox(12);
        bottom.setPadding(new Insets(12, 16, 12, 16));
        bottom.setAlignment(Pos.CENTER);
        bottom.setBackground(new Background(new BackgroundFill(Color.web("#121621"), CornerRadii.EMPTY, Insets.EMPTY)));

        Button btnSurrender = new Button("Xin thua");
        Button btnDraw = new Button("Cầu hòa");
        Button btnLeave = new Button("Thoát phòng");

        styleSecondaryButton(btnSurrender);
        styleSecondaryButton(btnDraw);
        styleSecondaryButton(btnLeave);

        btnSurrender.setPrefWidth(120);
        btnDraw.setPrefWidth(120);
        btnLeave.setPrefWidth(120);

        btnSurrender.setOnAction(e -> {
            if (new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc muốn xin thua?").showAndWait()
                    .orElse(null) == ButtonType.OK) {
                // TODO: Gửi packet xin thua lên server
                gameState.setGameOver(true);
                stopTimer();
                showResultAlert(GameState.RESULT_PLAYER2_WIN);
            }
        });

        btnDraw.setOnAction(e -> {
            // TODO: Gửi packet cầu hòa lên server
            new Alert(Alert.AlertType.INFORMATION, "Đã gửi yêu cầu cầu hòa đến đối thủ.").showAndWait();
        });

        btnLeave.setOnAction(e -> {
            if (new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc muốn thoát phòng?").showAndWait()
                    .orElse(null) == ButtonType.OK) {
                stopTimer();
                stage.setScene(createHomeScene(stage));
            }
        });

        bottom.getChildren().addAll(btnSurrender, btnDraw, btnLeave);
        root.setBottom(bottom);

        updateTimeLabels();
        updateTurnHighlight();

        Scene gameScene = new Scene(root, 1000, 1000);

        // Cập nhật kích thước canvas sau khi stage được show
        stage.setOnShown(e -> {
            double gridWidth = grid.getWidth();
            double gridHeight = grid.getHeight();
            drawLayer.setWidth(gridWidth);
            drawLayer.setHeight(gridHeight);
        });

        return gameScene;
    }

    private VBox createPlayerCard(String playerName, char symbol, Color avatarColor1, Color avatarColor2) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-background-color: #0F1419; " +
                        "-fx-border-color: #2A3F5F; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12;");

        // Header: Avatar + Name + Symbol
        HBox topSection = new HBox(16);
        topSection.setAlignment(Pos.CENTER_LEFT);

        // Avatar lớn hơn với glow
        StackPane avatarContainer = new StackPane();
        avatarContainer.setPrefSize(60, 60);
        avatarContainer.setAlignment(Pos.CENTER);

        Circle avatarBg = new Circle(30);
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, avatarColor1),
                new Stop(1, avatarColor2));
        avatarBg.setFill(gradient);

        // Glow effect mạnh
        DropShadow avatarGlow = new DropShadow();
        avatarGlow.setColor(avatarColor1.interpolate(avatarColor2, 0.5));
        avatarGlow.setRadius(12);
        avatarGlow.setSpread(0.5);
        avatarBg.setEffect(avatarGlow);

        // Symbol badge lớn hơn
        Label symbolBadge = new Label(String.valueOf(symbol));
        symbolBadge.setTextFill(Color.WHITE);
        symbolBadge.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        symbolBadge.setAlignment(Pos.CENTER);

        avatarContainer.getChildren().addAll(avatarBg, symbolBadge);

        // Thông tin người chơi (tên + ký hiệu)
        VBox infoBox = new VBox(4);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(playerName);
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Label symbolLabel = new Label("Ký hiệu: " + symbol);
        symbolLabel.setTextFill(Color.web("#888888"));
        symbolLabel.setFont(Font.font("Arial", 12));

        infoBox.getChildren().addAll(nameLabel, symbolLabel);

        topSection.getChildren().addAll(avatarContainer, infoBox);

        // Timer section: Time + Progress bar
        HBox timerSection = new HBox(12);
        timerSection.setAlignment(Pos.CENTER_LEFT);
        timerSection.setPadding(new Insets(8, 0, 0, 0));

        // Time label
        Label timeLabel = new Label("05:00");
        timeLabel.setTextFill(Color.WHITE);
        timeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        timeLabel.setPrefWidth(70);

        // Progress bar (horizontal)
        StackPane progressBar = new StackPane();
        progressBar.setPrefHeight(12);
        progressBar.setPrefWidth(180);
        progressBar.setStyle(
                "-fx-background-color: #1A2335; " +
                        "-fx-border-color: #2A3F5F; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6;");

        // Progress fill - Rectangle dạng bar (sẽ update width)
        javafx.scene.shape.Rectangle progressFill = new javafx.scene.shape.Rectangle();
        progressFill.setHeight(12);
        progressFill.setWidth(180); // Full width initially
        progressFill.setFill(Color.web("#4A90E2")); // Sẽ đổi màu dựa trên time
        progressFill.setArcWidth(6);
        progressFill.setArcHeight(6);

        progressBar.getChildren().add(progressFill);

        timerSection.getChildren().addAll(timeLabel, progressBar);

        card.getChildren().addAll(topSection, timerSection);

        // Lưu reference cho player 1 hoặc player 2
        if (symbol == 'X') {
            avatar1 = avatarBg;
            progressFill1 = progressFill; // Lưu để update width
            lblPlayer1Time = timeLabel;
        } else {
            avatar2 = avatarBg;
            progressFill2 = progressFill; // Lưu để update width
            lblPlayer2Time = timeLabel;
        }

        return card;
    }

    private void handleCellClick(int row, int col) {
        // Kiểm tra game đã kết thúc
        if (gameState.isGameOver()) {
            new Alert(Alert.AlertType.INFORMATION, "Ván cờ đã kết thúc!").showAndWait();
            return;
        }

        // Kiểm tra ô có thể click được
        if (!gameState.canPlace(row, col)) {
            new Alert(Alert.AlertType.WARNING, "Ô này đã có quân hoặc không hợp lệ!").showAndWait();
            return;
        }

        // Xác định symbol trước khi đổi lượt
        char symbol = gameState.isPlayer1Turn() ? 'X' : 'O';

        // Đặt quân
        gameState.placeMove(row, col);
        updateBoardCell(row, col, symbol, true);

        // Kiểm tra win
        int result = gameState.checkWin(row, col);
        if (result != GameState.RESULT_NONE) {
            gameState.setGameOver(true);
            stopTimer();
            showResultAlert(result);
            return;
        }

        // Chuyển lượt
        gameState.switchTurn();
        updateTurnHighlight();
    }

    public void updateBoardCell(int row, int col, char symbol, boolean highlight) {
        Button cell = cells[row][col];
        StackPane cellPane = cellPanes[row][col];

        // Xóa graphic cũ nếu có
        cell.setGraphic(null);
        cellPane.getChildren().removeIf(n -> n instanceof Line || (n instanceof Circle && n != cell));

        // Tạo Shape cho X hoặc O với glow effect
        Node piece = null;
        Color pieceColor = symbol == 'X' ? Color.web("#4A90E2") : Color.web("#50C878");

        if (symbol == 'X') {
            // Vẽ X bằng 2 đường Line
            double size = cell.getPrefWidth() * 0.6;
            double centerX = cell.getPrefWidth() / 2;
            double centerY = cell.getPrefHeight() / 2;

            Line line1 = new Line(centerX - size / 2, centerY - size / 2, centerX + size / 2, centerY + size / 2);
            Line line2 = new Line(centerX - size / 2, centerY + size / 2, centerX + size / 2, centerY - size / 2);

            line1.setStroke(pieceColor);
            line2.setStroke(pieceColor);
            line1.setStrokeWidth(4);
            line2.setStrokeWidth(4);
            line1.setStrokeLineCap(StrokeLineCap.ROUND);
            line2.setStrokeLineCap(StrokeLineCap.ROUND);

            // Glow effect cho X
            DropShadow xGlow = new DropShadow();
            xGlow.setColor(pieceColor);
            xGlow.setRadius(8);
            xGlow.setSpread(0.6);
            line1.setEffect(xGlow);
            line2.setEffect(xGlow);

            Group xGroup = new Group(line1, line2);
            cellPane.getChildren().add(xGroup);
            piece = xGroup;
        } else if (symbol == 'O') {
            // Vẽ O bằng Circle
            double radius = cell.getPrefWidth() * 0.25;
            Circle circle = new Circle(cell.getPrefWidth() / 2, cell.getPrefHeight() / 2, radius);
            circle.setFill(null);
            circle.setStroke(pieceColor);
            circle.setStrokeWidth(4);

            // Glow effect cho O
            DropShadow oGlow = new DropShadow();
            oGlow.setColor(pieceColor);
            oGlow.setRadius(8);
            oGlow.setSpread(0.6);
            circle.setEffect(oGlow);

            cellPane.getChildren().add(circle);
            piece = circle;
        }

        // Highlight nước đi cuối với viền sáng
        if (highlight) {
            resetBoardHighlight();

            // Viền sáng cho ô vừa đánh
            Border highlightBorder = new Border(new BorderStroke(
                    Color.web("#FFD700"),
                    BorderStrokeStyle.SOLID,
                    CornerRadii.EMPTY,
                    new BorderWidths(2)));
            cell.setBorder(highlightBorder);

            // Glow effect cho border
            DropShadow borderGlow = new DropShadow();
            borderGlow.setColor(Color.web("#FFD700"));
            borderGlow.setRadius(6);
            borderGlow.setSpread(0.5);
            cell.setEffect(borderGlow);
        }

        // Animation fade-in và scale khi xuất hiện symbol
        if (piece != null) {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(250), piece);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(250), piece);
            scaleIn.setFromX(0.3);
            scaleIn.setFromY(0.3);
            scaleIn.setToX(1);
            scaleIn.setToY(1);
            scaleIn.setInterpolator(Interpolator.EASE_OUT);

            fadeIn.play();
            scaleIn.play();
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

                // Reset border và effect
                Border cellBorder = new Border(new BorderStroke(
                        Color.web("#2A2F3E"),
                        BorderStrokeStyle.SOLID,
                        CornerRadii.EMPTY,
                        new BorderWidths(0.5)));
                cell.setBorder(cellBorder);

                DropShadow cellShadow = new DropShadow();
                cellShadow.setColor(Color.color(0, 0, 0, 0.2));
                cellShadow.setRadius(2);
                cell.setEffect(cellShadow);
            }
        }
    }

    public void updateTurnHighlight() {
        boolean p1Turn = gameState.isPlayer1Turn();

        // Highlight player card đang active với border và glow effect
        Color activeBorder = Color.web("#3399FF");
        Color normalBorder = Color.web("#2A2F3E");
        Color activeBg = Color.web("#253041");
        Color normalBg = Color.web("#1D2433");

        if (p1Box != null) {
            p1Box.setBackground(new Background(new BackgroundFill(
                    p1Turn ? activeBg : normalBg,
                    new CornerRadii(8),
                    Insets.EMPTY)));
            p1Box.setBorder(new Border(new BorderStroke(
                    p1Turn ? activeBorder : normalBorder,
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(8),
                    new BorderWidths(p1Turn ? 2 : 1))));
            if (p1Turn && avatar1 != null) {
                avatar1.setEffect(new Glow(0.5));
            } else if (avatar1 != null) {
                avatar1.setEffect(new DropShadow(8, Color.color(0, 0, 0, 0.3)));
            }
        }

        if (p2Box != null) {
            p2Box.setBackground(new Background(new BackgroundFill(
                    !p1Turn ? activeBg : normalBg,
                    new CornerRadii(8),
                    Insets.EMPTY)));
            p2Box.setBorder(new Border(new BorderStroke(
                    !p1Turn ? activeBorder : normalBorder,
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(8),
                    new BorderWidths(!p1Turn ? 2 : 1))));
            if (!p1Turn && avatar2 != null) {
                avatar2.setEffect(new Glow(0.5));
            } else if (avatar2 != null) {
                avatar2.setEffect(new DropShadow(8, Color.color(0, 0, 0, 0.3)));
            }
        }
    }

    private void startTimer() {
        if (timeline != null) {
            timeline.stop();
        }
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
        timeline.playFromStart();
    }

    public void stopTimer() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    private void updateTimeLabels() {
        long p1Time = gameState.getPlayer1RemainingMillis();
        long p2Time = gameState.getPlayer2RemainingMillis();
        lblPlayer1Time.setText(formatMillis(p1Time));
        lblPlayer2Time.setText(formatMillis(p2Time));

        // Cập nhật progress bar cho player 1
        if (progressFill1 != null) {
            double progress1 = Math.max(0, Math.min(1, p1Time / (INITIAL_TIME_SECONDS * 1000)));
            double barWidth = 180 * progress1;
            progressFill1.setWidth(barWidth);

            // Đổi màu theo thời gian còn lại
            if (p1Time < 10000) { // < 10 giây - cảnh báo đỏ
                progressFill1.setFill(Color.web("#FF0000"));
                lblPlayer1Time.setTextFill(Color.web("#FF4444"));
            } else if (p1Time < 60000) { // < 1 phút
                progressFill1.setFill(Color.web("#FF8844"));
                lblPlayer1Time.setTextFill(Color.WHITE);
            } else if (p1Time < 120000) { // < 2 phút
                progressFill1.setFill(Color.web("#FFAA00"));
                lblPlayer1Time.setTextFill(Color.WHITE);
            } else {
                progressFill1.setFill(Color.web("#4A90E2"));
                lblPlayer1Time.setTextFill(Color.WHITE);
            }
        }

        // Cập nhật progress bar cho player 2
        if (progressFill2 != null) {
            double progress2 = Math.max(0, Math.min(1, p2Time / (INITIAL_TIME_SECONDS * 1000)));
            double barWidth = 180 * progress2;
            progressFill2.setWidth(barWidth);

            // Đổi màu theo thời gian còn lại
            if (p2Time < 10000) { // < 10 giây - cảnh báo đỏ
                progressFill2.setFill(Color.web("#FF0000"));
                lblPlayer2Time.setTextFill(Color.web("#FF4444"));
            } else if (p2Time < 60000) { // < 1 phút
                progressFill2.setFill(Color.web("#FF8844"));
                lblPlayer2Time.setTextFill(Color.WHITE);
            } else if (p2Time < 120000) { // < 2 phút
                progressFill2.setFill(Color.web("#FFAA00"));
                lblPlayer2Time.setTextFill(Color.WHITE);
            } else {
                progressFill2.setFill(Color.web("#50C878"));
                lblPlayer2Time.setTextFill(Color.WHITE);
            }
        }
    }

    private String formatMillis(long millis) {
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void showResultAlert(int result) {
        String message;
        switch (result) {
            case GameState.RESULT_PLAYER1_WIN:
                message = "Người chơi " + gameState.getPlayer1Name() + " thắng!";
                break;
            case GameState.RESULT_PLAYER2_WIN:
                message = "Đối thủ thắng!";
                break;
            case GameState.RESULT_DRAW:
                message = "Hòa!";
                break;
            default:
                message = "Kết thúc ván!";
        }
        new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
    }

    private void drawWinningLineNeon(int lastRow, int lastCol) {
        // Lấy hướng chiến thắng từ GameState
        int startRow = gameState.getWinningLineRow();
        int startCol = gameState.getWinningLineCol();
        int dr = gameState.getWinningLineDR();
        int dc = gameState.getWinningLineDC();

        // Tìm điểm bắt đầu của 5 quân thắng (đi theo hướng âm)
        int r = startRow - dr;
        int c = startCol - dc;

        // Đi ngược hướng để tìm điểm bắt đầu của chuỗi 5 quân
        while (r >= 0 && c >= 0 && r < gameState.getBoardSize() && c < gameState.getBoardSize()
                && gameState.getCell(r, c) != GameState.EMPTY) {
            r -= dr;
            c -= dc;
        }

        // Bây giờ (r, c) là vị trí nằm ngoài chuỗi, nên quân đầu tiên ở (r + dr, c +
        // dc)
        int firstRow = r + dr;
        int firstCol = c + dc;

        // Xác định màu dựa trên ai vừa chiến thắng
        char lastSymbol = gameState.getSymbolAt(lastRow, lastCol);
        Color neonColor = (lastSymbol == 'X') ? Color.web("#4A90E2") : Color.web("#50C878");
        Color glowColor = (lastSymbol == 'X') ? Color.web("#6AB5FF") : Color.web("#7FE5A0");

        // Nhấp nháy 5 quân chiến thắng
        for (int i = 0; i < 5; i++) {
            int row = firstRow + (i * dr);
            int col = firstCol + (i * dc);

            if (row >= 0 && row < gameState.getBoardSize() && col >= 0 && col < gameState.getBoardSize()) {
                // Tạo chấm đỏ nhấp nháy cho ô chiến thắng
                highlightWinningCell(row, col, neonColor, glowColor);
            }
        }
    }

    private void highlightWinningCell(int row, int col, Color neonColor, Color glowColor) {
        Button cell = cells[row][col];
        StackPane cellPane = cellPanes[row][col];

        // Tạo chấm đỏ nhấp nháy ở trung tâm ô vuông
        Circle winningDot = new Circle(7); // Kích thước lớn hơn để thấy rõ
        winningDot.setFill(Color.web("#FF4444")); // Màu đỏ
        winningDot.setStroke(Color.WHITE);
        winningDot.setStrokeWidth(1);

        // Glow effect mạnh với màu đỏ
        DropShadow dotGlow = new DropShadow();
        dotGlow.setColor(Color.web("#FF6666")); // Glow đỏ nhạt hơn
        dotGlow.setRadius(12);
        dotGlow.setSpread(0.8);
        winningDot.setEffect(dotGlow);

        // Đặt vị trí ở trung tâm của cell
        StackPane.setAlignment(winningDot, javafx.geometry.Pos.CENTER);

        // Animation nhấp nháy
        Timeline blinkTimeline = new Timeline(
                new KeyFrame(Duration.millis(500),
                        new KeyValue(winningDot.opacityProperty(), 0.2)),
                new KeyFrame(Duration.millis(1000),
                        new KeyValue(winningDot.opacityProperty(), 1.0)));
        blinkTimeline.setCycleCount(Timeline.INDEFINITE);

        // Xóa chấm cũ nếu có
        cellPane.getChildren().removeIf(n -> n instanceof Circle && n != cell);

        // Thêm chấm vào cell (sẽ ở phía trên các element khác)
        cellPane.getChildren().add(winningDot);

        // Chạy animation
        blinkTimeline.play();
    }

    private void stylePrimaryButton(Button button) {
        button.setTextFill(Color.WHITE);
        button.setBackground(
                new Background(new BackgroundFill(Color.web("#2D89EF"), new CornerRadii(4), Insets.EMPTY)));
    }

    private void styleSecondaryButton(Button button) {
        button.setTextFill(Color.WHITE);
        button.setBackground(
                new Background(new BackgroundFill(Color.web("#363B4A"), new CornerRadii(4), Insets.EMPTY)));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
