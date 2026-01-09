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
import javafx.animation.RotateTransition;
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
    private Stage loadingDialogStage; // Loading dialog stage

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
            
            // Fade transition khi chuyển scene
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), primaryStage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                primaryStage.setScene(gameScene);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), gameScene.getRoot());
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
                startTimer();
            });
            fadeOut.play();
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
                showCustomWarningDialog("Cảnh báo", "Vui lòng nhập tên người chơi.");
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

        HBox top = new HBox(8);
        top.setPadding(new Insets(10, 16, 10, 16));
        top.setAlignment(Pos.CENTER);
        top.setBackground(new Background(new BackgroundFill(Color.web("#121621"), CornerRadii.EMPTY, Insets.EMPTY)));

        // Player 1 Card với Avatar
        p1Box = createPlayerCard(
                gameState.getPlayer1Name(),
                'X',
                Color.web("#FF4444"), // Màu đỏ cho devil icon
                Color.web("#CC0000"));
        p1Box.setId("player1Card");

        // Center section với điểm số và timer tổng
        VBox centerSection = createCenterSection();

        // Player 2 Card với Avatar
        p2Box = createPlayerCard(
                gameState.getPlayer2Name(),
                'O',
                Color.web("#50C878"), // Màu xanh lá cho alien icon
                Color.web("#3FA568"));
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

                // Hover effects với hiệu ứng nổi mạnh hơn và preview symbol
                cell.setOnMouseEntered(e -> {
                    if (cell.getGraphic() == null && !gameState.isGameOver() && gameState.isLocalPlayersTurn()) {
                        Color hoverBg = isLight ? Color.web("#222836") : Color.web("#1A1F28");
                        cell.setBackground(
                                new Background(new BackgroundFill(hoverBg, CornerRadii.EMPTY, Insets.EMPTY)));
                        DropShadow hoverShadow = new DropShadow();
                        hoverShadow.setColor(Color.web("#4A90E2"));
                        hoverShadow.setRadius(6);
                        hoverShadow.setSpread(0.4);
                        cell.setEffect(hoverShadow);
                        
                        // Preview symbol khi hover (mờ)
                        char previewSymbol = gameState.getLocalPlayerSymbol();
                        Label previewLabel = new Label(String.valueOf(previewSymbol));
                        previewLabel.setTextFill(Color.web("#FFFFFF", 0.3));
                        previewLabel.setFont(Font.font("Arial", FontWeight.BOLD, (int)(cellSize * 0.4)));
                        cell.setGraphic(previewLabel);
                    }
                });
                cell.setOnMouseExited(e -> {
                    if (cell.getGraphic() != null && cell.getGraphic() instanceof Label) {
                        cell.setGraphic(null);
                    }
                    if (cell.getGraphic() == null || cell.getGraphic() instanceof Label) {
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
        HBox bottom = new HBox(10);
        bottom.setPadding(new Insets(10, 16, 10, 16));
        bottom.setAlignment(Pos.CENTER);
        bottom.setBackground(new Background(new BackgroundFill(Color.web("#121621"), CornerRadii.EMPTY, Insets.EMPTY)));

        Button btnSurrender = createModernButton("🏳️ Xin thua", Color.web("#FF4444"), Color.web("#CC0000"));
        Button btnDraw = createModernButton("🤝 Cầu hòa", Color.web("#4A90E2"), Color.web("#357ABD"));
        Button btnLeave = createModernButton("🚪 Thoát", Color.web("#888888"), Color.web("#666666"));

        btnSurrender.setPrefWidth(140);
        btnDraw.setPrefWidth(140);
        btnLeave.setPrefWidth(140);
        
        // Tooltip cho các nút
        javafx.scene.control.Tooltip.install(btnSurrender, new javafx.scene.control.Tooltip("Xin thua và kết thúc ván cờ"));
        javafx.scene.control.Tooltip.install(btnDraw, new javafx.scene.control.Tooltip("Gửi yêu cầu cầu hòa đến đối thủ"));
        javafx.scene.control.Tooltip.install(btnLeave, new javafx.scene.control.Tooltip("Thoát khỏi phòng chơi"));

        btnSurrender.setOnAction(e -> {
            if (showCustomConfirmDialog("Xác nhận", "Bạn có chắc muốn xin thua?")) {
                controller.onSurrender();
            }
        });

        btnDraw.setOnAction(e -> {
            controller.onDrawRequest();
            showCustomInfoDialog("Thông báo", "Đã gửi yêu cầu cầu hòa đến đối thủ.");
        });

        btnLeave.setOnAction(e -> {
            if (showCustomConfirmDialog("Xác nhận", "Bạn có chắc muốn thoát phòng?")) {
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

    private VBox createPlayerCard(String playerName, char symbol, Color iconColor1, Color iconColor2) {
        HBox card = new HBox(12);
        card.setPadding(new Insets(8, 16, 8, 16));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setId("playerCard_" + symbol); // Thêm ID để dễ tìm
        card.setStyle(
                "-fx-background-color: #0F1419; " +
                        "-fx-border-color: #2A3F5F; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8;");

        // Icon (Devil cho X, Alien cho O)
        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(40, 40);
        iconContainer.setAlignment(Pos.CENTER);

        Circle iconBg = new Circle(20);
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, iconColor1),
                new Stop(1, iconColor2));
        iconBg.setFill(gradient);

        // Glow effect
        DropShadow iconGlow = new DropShadow();
        iconGlow.setColor(iconColor1.interpolate(iconColor2, 0.5));
        iconGlow.setRadius(8);
        iconGlow.setSpread(0.4);
        iconBg.setEffect(iconGlow);

        // Icon text (emoji hoặc symbol đơn giản)
        Label iconLabel = new Label(symbol == 'X' ? "👹" : "👽");
        iconLabel.setFont(Font.font("Arial", 20));
        iconLabel.setAlignment(Pos.CENTER);

        iconContainer.getChildren().addAll(iconBg, iconLabel);

        // Thông tin người chơi
        VBox infoBox = new VBox(2);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(playerName);
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        // Time label
        Label timeLabel = new Label("05:00");
        timeLabel.setTextFill(Color.web("#B0B0B0"));
        timeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        infoBox.getChildren().addAll(nameLabel, timeLabel);

        card.getChildren().addAll(iconContainer, infoBox);

        // Lưu reference cho player 1 hoặc player 2
        if (symbol == 'X') {
            avatar1 = iconBg;
            lblPlayer1Time = timeLabel;
        } else {
            avatar2 = iconBg;
            lblPlayer2Time = timeLabel;
        }

        return new VBox(card);
    }

    private VBox createCenterSection() {
        VBox center = new VBox(6);
        center.setPadding(new Insets(8, 20, 8, 20));
        center.setAlignment(Pos.CENTER);
        center.setStyle(
                "-fx-background-color: #0F1419; " +
                        "-fx-border-color: #2A3F5F; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8;");

        // Score section
        HBox scoreBox = new HBox(16);
        scoreBox.setAlignment(Pos.CENTER);

        // Crown icon với điểm số
        HBox player1Score = new HBox(6);
        player1Score.setAlignment(Pos.CENTER);
        Label crownIcon = new Label("👑");
        crownIcon.setFont(Font.font("Arial", 18));
        Label score1Label = new Label("0");
        score1Label.setTextFill(Color.WHITE);
        score1Label.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        player1Score.getChildren().addAll(crownIcon, score1Label);

        // Skull icon với điểm số
        HBox player2Score = new HBox(6);
        player2Score.setAlignment(Pos.CENTER);
        Label skullIcon = new Label("💀");
        skullIcon.setFont(Font.font("Arial", 18));
        Label score2Label = new Label("0");
        score2Label.setTextFill(Color.WHITE);
        score2Label.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        player2Score.getChildren().addAll(skullIcon, score2Label);

        scoreBox.getChildren().addAll(player1Score, player2Score);

        center.getChildren().addAll(scoreBox);

        return center;
    }

    private void handleCellClick(int row, int col) {
        // Kiểm tra game đã kết thúc
        if (gameState.isGameOver()) {
            showCustomInfoDialog("Thông báo", "Ván cờ đã kết thúc!");
            return;
        }

        // Kiểm tra ô có thể click được
        if (!gameState.canPlace(row, col)) {
            showCustomWarningDialog("Cảnh báo", "Ô này đã có quân hoặc không hợp lệ!");
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

        // Nếu highlight, reset tất cả highlight trước
        if (highlight) {
            resetBoardHighlight();
        }

        // Xóa quân cũ
        cellPane.getChildren().removeIf(n -> n instanceof Group || n instanceof Circle);

        Node piece = null;

        // Cấu hình hiệu ứng Glow (Phát sáng)
        DropShadow neonGlow = new DropShadow();
        neonGlow.setRadius(15);
        neonGlow.setSpread(0.4);

        if (symbol == 'X') {
            // Xanh Neon (#00E5FF)
            Color xColor = Color.web("#00E5FF");
            neonGlow.setColor(xColor);

            double size = cellSize * 0.5;
            Line line1 = new Line(-size/2, -size/2, size/2, size/2);
            Line line2 = new Line(-size/2, size/2, size/2, -size/2);

            // Style cho nét vẽ
            for (Line line : new Line[]{line1, line2}) {
                line.setStroke(xColor);
                line.setStrokeWidth(4);
                line.setStrokeLineCap(StrokeLineCap.ROUND);
                line.setEffect(neonGlow); // Áp dụng Glow
            }

            Group xGroup = new Group(line1, line2);
            cellPane.getChildren().add(xGroup);
            piece = xGroup;

        } else if (symbol == 'O') {
            // Xanh Lá Neon (#00E676)
            Color oColor = Color.web("#00E676");
            neonGlow.setColor(oColor);

            double radius = cellSize * 0.3;
            Circle circle = new Circle(radius);
            circle.setFill(null); // Rỗng ruột
            circle.setStroke(oColor);
            circle.setStrokeWidth(4);
            circle.setEffect(neonGlow); // Áp dụng Glow

            cellPane.getChildren().add(circle);
            piece = circle;
        }

        // Highlight nước đi mới nhất với viền vàng sáng và glow effect
        if (highlight) {
            // Viền vàng sáng cho ô vừa đánh
            Border highlightBorder = new Border(new BorderStroke(
                    Color.web("#FFD700"),
                    BorderStrokeStyle.SOLID,
                    CornerRadii.EMPTY,
                    new BorderWidths(3)));
            cell.setBorder(highlightBorder);

            // Glow effect mạnh cho border (vàng)
            DropShadow borderGlow = new DropShadow();
            borderGlow.setColor(Color.web("#FFD700"));
            borderGlow.setRadius(12);
            borderGlow.setSpread(0.6);
            cell.setEffect(borderGlow);

            // Animation pulse để thu hút sự chú ý - thay đổi opacity của cell
            FadeTransition pulseAnimation = new FadeTransition(Duration.millis(600), cell);
            pulseAnimation.setFromValue(1.0);
            pulseAnimation.setToValue(0.7);
            pulseAnimation.setCycleCount(4); // Nhấp nháy 4 lần (2 chu kỳ)
            pulseAnimation.setAutoReverse(true);
            pulseAnimation.play();
        }

        // Hiệu ứng Fade-in + Scale + Bounce khi quân cờ xuất hiện
        if (piece != null) {
            // Fade in
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), piece);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            
            // Scale animation với bounce effect
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), piece);
            scaleIn.setFromX(0.3);
            scaleIn.setFromY(0.3);
            scaleIn.setToX(1.1);
            scaleIn.setToY(1.1);
            scaleIn.setInterpolator(Interpolator.EASE_OUT);
            
            // Bounce back
            ScaleTransition bounceBack = new ScaleTransition(Duration.millis(200), piece);
            bounceBack.setFromX(1.1);
            bounceBack.setFromY(1.1);
            bounceBack.setToX(1.0);
            bounceBack.setToY(1.0);
            bounceBack.setInterpolator(Interpolator.EASE_IN);
            
            // Chạy animation tuần tự
            fadeIn.play();
            scaleIn.play();
            scaleIn.setOnFinished(e -> bounceBack.play());
        }
    }
//    public void updateBoardCell(int row, int col, char symbol, boolean highlight) {
//        Button cell = cells[row][col];
//        StackPane cellPane = cellPanes[row][col];
//
//        // Xóa graphic cũ nếu có
//        cell.setGraphic(null);
//        cellPane.getChildren().removeIf(n -> n instanceof Line || (n instanceof Circle && n != cell));
//
//        // Tạo Shape cho X hoặc O với glow effect
//        Node piece = null;
//        Color pieceColor = symbol == 'X' ? Color.web("#4A90E2") : Color.web("#50C878");
//
//        if (symbol == 'X') {
//            // Vẽ X bằng 2 đường Line
//            double size = cell.getPrefWidth() * 0.6;
//            double centerX = cell.getPrefWidth() / 2;
//            double centerY = cell.getPrefHeight() / 2;
//
//            Line line1 = new Line(centerX - size / 2, centerY - size / 2, centerX + size / 2, centerY + size / 2);
//            Line line2 = new Line(centerX - size / 2, centerY + size / 2, centerX + size / 2, centerY - size / 2);
//
//            line1.setStroke(pieceColor);
//            line2.setStroke(pieceColor);
//            line1.setStrokeWidth(4);
//            line2.setStrokeWidth(4);
//            line1.setStrokeLineCap(StrokeLineCap.ROUND);
//            line2.setStrokeLineCap(StrokeLineCap.ROUND);
//
//            // Glow effect cho X
//            DropShadow xGlow = new DropShadow();
//            xGlow.setColor(pieceColor);
//            xGlow.setRadius(8);
//            xGlow.setSpread(0.6);
//            line1.setEffect(xGlow);
//            line2.setEffect(xGlow);
//
//            Group xGroup = new Group(line1, line2);
//            cellPane.getChildren().add(xGroup);
//            piece = xGroup;
//        } else if (symbol == 'O') {
//            // Vẽ O bằng Circle
//            double radius = cell.getPrefWidth() * 0.25;
//            Circle circle = new Circle(cell.getPrefWidth() / 2, cell.getPrefHeight() / 2, radius);
//            circle.setFill(null);
//            circle.setStroke(pieceColor);
//            circle.setStrokeWidth(4);
//
//            // Glow effect cho O
//            DropShadow oGlow = new DropShadow();
//            oGlow.setColor(pieceColor);
//            oGlow.setRadius(8);
//            oGlow.setSpread(0.6);
//            circle.setEffect(oGlow);
//
//            cellPane.getChildren().add(circle);
//            piece = circle;
//        }
//
//        // Highlight nước đi cuối với viền sáng
//        if (highlight) {
//            resetBoardHighlight();
//
//            // Viền sáng cho ô vừa đánh
//            Border highlightBorder = new Border(new BorderStroke(
//                    Color.web("#FFD700"),
//                    BorderStrokeStyle.SOLID,
//                    CornerRadii.EMPTY,
//                    new BorderWidths(2)));
//            cell.setBorder(highlightBorder);
//
//            // Glow effect cho border
//            DropShadow borderGlow = new DropShadow();
//            borderGlow.setColor(Color.web("#FFD700"));
//            borderGlow.setRadius(6);
//            borderGlow.setSpread(0.5);
//            cell.setEffect(borderGlow);
//        }
//
//        // Animation fade-in và scale khi xuất hiện symbol
//        if (piece != null) {
//            FadeTransition fadeIn = new FadeTransition(Duration.millis(250), piece);
//            fadeIn.setFromValue(0);
//            fadeIn.setToValue(1);
//
//            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(250), piece);
//            scaleIn.setFromX(0.3);
//            scaleIn.setFromY(0.3);
//            scaleIn.setToX(1);
//            scaleIn.setToY(1);
//            scaleIn.setInterpolator(Interpolator.EASE_OUT);
//
//            fadeIn.play();
//            scaleIn.play();
//        }
//    }

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
        if (p1Box != null && p1Box.getChildren().size() > 0) {
            Node cardNode = p1Box.getChildren().get(0);
            if (cardNode instanceof HBox) {
                HBox card = (HBox) cardNode;
                card.setStyle(
                        "-fx-background-color: " + (p1Turn ? "#1A2335" : "#0F1419") + "; " +
                                "-fx-border-color: " + (p1Turn ? "#3399FF" : "#2A3F5F") + "; " +
                                "-fx-border-width: " + (p1Turn ? "2" : "1") + "; " +
                                "-fx-border-radius: 8; " +
                                "-fx-background-radius: 8;");
            }
            
            if (p1Turn && avatar1 != null) {
                Glow glow = new Glow(0.6);
                avatar1.setEffect(glow);
            } else if (avatar1 != null) {
                DropShadow shadow = new DropShadow();
                shadow.setColor(Color.color(0, 0, 0, 0.3));
                shadow.setRadius(8);
                avatar1.setEffect(shadow);
            }
        }

        if (p2Box != null && p2Box.getChildren().size() > 0) {
            Node cardNode = p2Box.getChildren().get(0);
            if (cardNode instanceof HBox) {
                HBox card = (HBox) cardNode;
                card.setStyle(
                        "-fx-background-color: " + (!p1Turn ? "#1A2335" : "#0F1419") + "; " +
                                "-fx-border-color: " + (!p1Turn ? "#3399FF" : "#2A3F5F") + "; " +
                                "-fx-border-width: " + (!p1Turn ? "2" : "1") + "; " +
                                "-fx-border-radius: 8; " +
                                "-fx-background-radius: 8;");
            }
            
            if (!p1Turn && avatar2 != null) {
                Glow glow = new Glow(0.6);
                avatar2.setEffect(glow);
            } else if (avatar2 != null) {
                DropShadow shadow = new DropShadow();
                shadow.setColor(Color.color(0, 0, 0, 0.3));
                shadow.setRadius(8);
                avatar2.setEffect(shadow);
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
        
        if (lblPlayer1Time != null) {
            lblPlayer1Time.setText(formatMillis(p1Time));
            // Đổi màu theo thời gian còn lại
            if (p1Time < 10000) { // < 10 giây - cảnh báo đỏ
                lblPlayer1Time.setTextFill(Color.web("#FF4444"));
            } else if (p1Time < 60000) { // < 1 phút
                lblPlayer1Time.setTextFill(Color.web("#FF8844"));
            } else {
                lblPlayer1Time.setTextFill(Color.web("#B0B0B0"));
            }
        }

        if (lblPlayer2Time != null) {
            lblPlayer2Time.setText(formatMillis(p2Time));
            // Đổi màu theo thời gian còn lại
            if (p2Time < 10000) { // < 10 giây - cảnh báo đỏ
                lblPlayer2Time.setTextFill(Color.web("#FF4444"));
            } else if (p2Time < 60000) { // < 1 phút
                lblPlayer2Time.setTextFill(Color.web("#FF8844"));
            } else {
                lblPlayer2Time.setTextFill(Color.web("#B0B0B0"));
            }
        }
    }

    private String formatMillis(long millis) {
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    // Public methods để ClientController có thể gọi custom dialogs
    public void showCustomErrorDialog(String title, String message) {
        Platform.runLater(() -> {
            Stage dialogStage = new Stage();
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.setTitle(title);
            dialogStage.initStyle(javafx.stage.StageStyle.UNDECORATED);

            VBox dialogVBox = new VBox(20);
            dialogVBox.setPadding(new Insets(24));
            dialogVBox.setAlignment(Pos.CENTER);
            dialogVBox.setStyle(
                    "-fx-background-color: #1A1F2E; " +
                            "-fx-border-color: #FF4444; " +
                            "-fx-border-width: 2; " +
                            "-fx-border-radius: 12; " +
                            "-fx-background-radius: 12;");

            Label titleLabel = new Label(title);
            titleLabel.setTextFill(Color.WHITE);
            titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));

            Label messageLabel = new Label(message);
            messageLabel.setTextFill(Color.web("#B0B0B0"));
            messageLabel.setFont(Font.font("Arial", 14));
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(300);

            Button btnOK = createModernButton("OK", Color.web("#FF4444"), Color.web("#CC0000"));
            btnOK.setPrefWidth(100);
            btnOK.setOnAction(e -> dialogStage.close());

            dialogVBox.getChildren().addAll(titleLabel, messageLabel, btnOK);

            Scene dialogScene = new Scene(dialogVBox);
            dialogScene.setFill(Color.TRANSPARENT);
            dialogStage.setScene(dialogScene);
            dialogStage.showAndWait();
        });
    }

    public void showCustomMessageDialog(String message) {
        Platform.runLater(() -> {
            showCustomInfoDialog("Thông báo", message);
        });
    }

    public void showCustomDrawRequestDialog(Runnable onAccepted, Runnable onRejected) {
        Platform.runLater(() -> {
            boolean accepted = showCustomConfirmDialog("Yêu cầu cầu hòa", "Đối thủ yêu cầu cầu hòa. Bạn có đồng ý?");
            if (accepted) {
                onAccepted.run();
            } else {
                onRejected.run();
            }
        });
    }

    public void showLoadingDialog(String title, String message) {
        Platform.runLater(() -> {
            if (loadingDialogStage != null) {
                loadingDialogStage.close();
            }
            
            loadingDialogStage = new Stage();
            loadingDialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            loadingDialogStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            if (primaryStage != null) {
                loadingDialogStage.initOwner(primaryStage);
            }
            
            VBox dialogVBox = new VBox(20);
            dialogVBox.setPadding(new Insets(30));
            dialogVBox.setAlignment(Pos.CENTER);
            dialogVBox.setStyle(
                    "-fx-background-color: #1A1F2E; " +
                            "-fx-border-color: #4A90E2; " +
                            "-fx-border-width: 2; " +
                            "-fx-border-radius: 12; " +
                            "-fx-background-radius: 12;");
            
            // Spinner animation
            Circle spinner = new Circle(20);
            spinner.setFill(null);
            spinner.setStroke(Color.web("#4A90E2"));
            spinner.setStrokeWidth(3);
            
            RotateTransition rotate = new RotateTransition(Duration.millis(1000), spinner);
            rotate.setByAngle(360);
            rotate.setCycleCount(Timeline.INDEFINITE);
            rotate.play();
            
            Label titleLabel = new Label(title);
            titleLabel.setTextFill(Color.WHITE);
            titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            
            Label messageLabel = new Label(message);
            messageLabel.setTextFill(Color.web("#B0B0B0"));
            messageLabel.setFont(Font.font("Arial", 13));
            
            dialogVBox.getChildren().addAll(spinner, titleLabel, messageLabel);
            
            Scene dialogScene = new Scene(dialogVBox);
            dialogScene.setFill(Color.TRANSPARENT);
            loadingDialogStage.setScene(dialogScene);
            loadingDialogStage.show();
        });
    }
    
    public void hideLoadingDialog() {
        Platform.runLater(() -> {
            if (loadingDialogStage != null) {
                loadingDialogStage.close();
                loadingDialogStage = null;
            }
        });
    }

    public void showResultAlert(int result) {
        String message;
        String title = "Kết quả";
        Color borderColor = Color.web("#4A90E2");
        
        switch (result) {
            case GameState.RESULT_PLAYER1_WIN:
                message = "Người chơi " + gameState.getPlayer1Name() + " thắng!";
                borderColor = Color.web("#FFD700");
                break;
            case GameState.RESULT_PLAYER2_WIN:
                message = "Đối thủ thắng!";
                borderColor = Color.web("#FF4444");
                break;
            case GameState.RESULT_DRAW:
                message = "Hòa!";
                borderColor = Color.web("#50C878");
                break;
            default:
                message = "Kết thúc ván!";
        }
        showCustomResultDialog(title, message, borderColor);
    }

    private void showCustomResultDialog(String title, String message, Color borderColor) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialogStage.setTitle(title);
        dialogStage.initStyle(javafx.stage.StageStyle.UNDECORATED);

        VBox dialogVBox = new VBox(20);
        dialogVBox.setPadding(new Insets(24));
        dialogVBox.setAlignment(Pos.CENTER);
        dialogVBox.setStyle(
                "-fx-background-color: #1A1F2E; " +
                        "-fx-border-color: " + toHexColor(borderColor) + "; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12;");

        Label titleLabel = new Label(title);
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        Label messageLabel = new Label(message);
        messageLabel.setTextFill(Color.WHITE);
        messageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(350);
        messageLabel.setAlignment(Pos.CENTER);

        Button btnOK = createModernButton("OK", borderColor, borderColor.deriveColor(0, 1, 0.8, 1));
        btnOK.setPrefWidth(120);
        btnOK.setOnAction(e -> dialogStage.close());

        dialogVBox.getChildren().addAll(titleLabel, messageLabel, btnOK);

        Scene dialogScene = new Scene(dialogVBox);
        dialogScene.setFill(Color.TRANSPARENT);
        dialogStage.setScene(dialogScene);
        dialogStage.showAndWait();
    }

    private void showCustomWarningDialog(String title, String message) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialogStage.setTitle(title);
        dialogStage.initStyle(javafx.stage.StageStyle.UNDECORATED);

        VBox dialogVBox = new VBox(20);
        dialogVBox.setPadding(new Insets(24));
        dialogVBox.setAlignment(Pos.CENTER);
        dialogVBox.setStyle(
                "-fx-background-color: #1A1F2E; " +
                        "-fx-border-color: #FF8844; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12;");

        Label titleLabel = new Label(title);
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Label messageLabel = new Label(message);
        messageLabel.setTextFill(Color.web("#B0B0B0"));
        messageLabel.setFont(Font.font("Arial", 14));
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300);

        Button btnOK = createModernButton("OK", Color.web("#FF8844"), Color.web("#CC6600"));
        btnOK.setPrefWidth(100);
        btnOK.setOnAction(e -> dialogStage.close());

        dialogVBox.getChildren().addAll(titleLabel, messageLabel, btnOK);

        Scene dialogScene = new Scene(dialogVBox);
        dialogScene.setFill(Color.TRANSPARENT);
        dialogStage.setScene(dialogScene);
        dialogStage.showAndWait();
    }

    private String toHexColor(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
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

    private Button createModernButton(String text, Color color1, Color color2) {
        Button button = new Button(text);
        button.setTextFill(Color.WHITE);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        button.setPrefHeight(38);
        
        // Gradient background
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, color1),
                new Stop(1, color2));
        button.setBackground(new Background(new BackgroundFill(gradient, new CornerRadii(6), Insets.EMPTY)));
        
        // Border
        button.setBorder(new Border(new BorderStroke(
                Color.web("#FFFFFF", 0.2),
                BorderStrokeStyle.SOLID,
                new CornerRadii(6),
                new BorderWidths(1))));
        
        // Glow effect
        DropShadow glow = new DropShadow();
        glow.setColor(color1);
        glow.setRadius(6);
        glow.setSpread(0.3);
        button.setEffect(glow);
        
        // Hover effects
        button.setOnMouseEntered(e -> {
            LinearGradient hoverGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, color1.deriveColor(0, 1, 1.1, 1)),
                    new Stop(1, color2.deriveColor(0, 1, 1.1, 1)));
            button.setBackground(new Background(new BackgroundFill(hoverGradient, new CornerRadii(6), Insets.EMPTY)));
            DropShadow hoverGlow = new DropShadow();
            hoverGlow.setColor(color1);
            hoverGlow.setRadius(10);
            hoverGlow.setSpread(0.4);
            button.setEffect(hoverGlow);
        });
        
        button.setOnMouseExited(e -> {
            button.setBackground(new Background(new BackgroundFill(gradient, new CornerRadii(6), Insets.EMPTY)));
            button.setEffect(glow);
        });
        
        return button;
    }

    private boolean showCustomConfirmDialog(String title, String message) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialogStage.setTitle(title);
        dialogStage.initStyle(javafx.stage.StageStyle.UNDECORATED);

        VBox dialogVBox = new VBox(20);
        dialogVBox.setPadding(new Insets(24));
        dialogVBox.setAlignment(Pos.CENTER);
        dialogVBox.setStyle(
                "-fx-background-color: #1A1F2E; " +
                        "-fx-border-color: #4A90E2; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12;");

        Label titleLabel = new Label(title);
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Label messageLabel = new Label(message);
        messageLabel.setTextFill(Color.web("#B0B0B0"));
        messageLabel.setFont(Font.font("Arial", 14));
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300);

        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER);

        Button btnOK = createModernButton("Xác nhận", Color.web("#4A90E2"), Color.web("#357ABD"));
        Button btnCancel = createModernButton("Hủy", Color.web("#666666"), Color.web("#444444"));
        
        btnOK.setPrefWidth(100);
        btnCancel.setPrefWidth(100);

        final boolean[] result = {false};
        btnOK.setOnAction(e -> {
            result[0] = true;
            dialogStage.close();
        });
        btnCancel.setOnAction(e -> dialogStage.close());

        buttonBox.getChildren().addAll(btnOK, btnCancel);
        dialogVBox.getChildren().addAll(titleLabel, messageLabel, buttonBox);

        Scene dialogScene = new Scene(dialogVBox);
        dialogScene.setFill(Color.TRANSPARENT);
        dialogStage.setScene(dialogScene);
        dialogStage.showAndWait();

        return result[0];
    }

    private void showCustomInfoDialog(String title, String message) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialogStage.setTitle(title);
        dialogStage.initStyle(javafx.stage.StageStyle.UNDECORATED);

        VBox dialogVBox = new VBox(20);
        dialogVBox.setPadding(new Insets(24));
        dialogVBox.setAlignment(Pos.CENTER);
        dialogVBox.setStyle(
                "-fx-background-color: #1A1F2E; " +
                        "-fx-border-color: #50C878; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12;");

        Label titleLabel = new Label(title);
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Label messageLabel = new Label(message);
        messageLabel.setTextFill(Color.web("#B0B0B0"));
        messageLabel.setFont(Font.font("Arial", 14));
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300);

        Button btnOK = createModernButton("OK", Color.web("#50C878"), Color.web("#3FA568"));
        btnOK.setPrefWidth(100);
        btnOK.setOnAction(e -> dialogStage.close());

        dialogVBox.getChildren().addAll(titleLabel, messageLabel, btnOK);

        Scene dialogScene = new Scene(dialogVBox);
        dialogScene.setFill(Color.TRANSPARENT);
        dialogStage.setScene(dialogScene);
        dialogStage.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
