package MrKagabond.painter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.paint.Color;

public class PaintFileParser {

    private int lineNumber = 0;
    private String errorMessage = "";
    private PaintModel paintModel;

    private boolean isCircle, isRectangle, isSquiggle, isPolygon = false;

    private Pattern pFileStart = Pattern.compile("^(Paint Save File Version 1.0)|(PaintSaveFileVersion1.0)$");
    private Pattern pFileEnd = Pattern.compile("^(End Paint Save File)|(EndPaintSaveFile)$");

    private Pattern pAnyShape = Pattern.compile("^(Circle)||(Rectangle)||(Squiggle)||(Polygon)$");

    private Pattern pGeneralColor = Pattern.compile(
            "^\tcolor:(([0-9]{0,2})|([0-2]{0,1}[0-5]{0,2})|([0-1]{0,1}[0-9]{0,2})|([0-2]{0,1}[0-5]{0,1}[0-9]{0,1}))(,)(([0-9]{0,2})|([0-2]{0,1}[0-5]{0,2})|([0-1]{0,1}[0-9]{0,2})|([0-2]{0,1}[0-5]{0,1}[0-9]{0,1}))(,)(([0-9]{0,2})|([0-2]{0,1}[0-5]{0,2})|([0-1]{0,1}[0-9]{0,2})|([0-2]{0,1}[0-5]{0,1}[0-9]{0,1}))$");
    private Pattern pGeneralIsFilled = Pattern.compile("^\t(filled:)(true|false)$");

    private Pattern pCircleStart = Pattern.compile("^Circle$");
    private Pattern pCircleCenter = Pattern.compile("^\t(center:\\()[0-9]+(,)[0-9]+(\\))$");
    private Pattern pCircleRadius = Pattern.compile("^\t(radius:)[0-9]+$");
    private Pattern pCircleEnd = Pattern.compile("^End Circle$");

    private Pattern pRectangleStart = Pattern.compile("^Rectangle$");
    private Pattern pRectangleP1 = Pattern.compile("^\t(p1:)\\([0-9]+(,)[0-9]+(\\))$");
    private Pattern pRectangleP2 = Pattern.compile("^\t(p2:)\\([0-9]+(,)[0-9]+(\\))$");
    private Pattern pRectangleEnd = Pattern.compile("^End Rectangle$");

    private Pattern pSquiggleStart = Pattern.compile("^Squiggle$");
    private Pattern pSquigglePointsStart = Pattern.compile("^\tpoints$");
    private Pattern pSquigglePoint = Pattern.compile("^\t\t(point:\\()[0-9]+(,)[0-9]+(\\))$");
    private Pattern pSquigglePointsEnd = Pattern.compile("^\tend points$");
    private Pattern pSquiggleEnd = Pattern.compile("^End Squiggle$");

    private Pattern pPolygonStart = Pattern.compile("^Polyline$");
    private Pattern pPolygonPointsStart = Pattern.compile("^\tpoints$");
    private Pattern pPolygonPoint = Pattern.compile("^\t\t(point:\\()[0-9]+(,)[0-9]+(\\))$");
    private Pattern pPolygonPointsEnd = Pattern.compile("^\tend points$");
    private Pattern pPolygonEnd = Pattern.compile("^End Polyline$");

    private Color shapeColor = null;
    private boolean shapeIsFilled = false;
    private int shapeRadius = -1;
    private Point shapeCenter, shapeP1, shapeP2 = null;
    private ArrayList<Point> shapePoints = new ArrayList<Point>();

    /**
     * Store an appropriate error message in this, including lineNumber where
     * the error occurred.
     *
     * @param mesg
     */
    private void error(String mesg) {
        this.errorMessage = "Error in line " + lineNumber + " " + mesg;
    }

    /**
     *
     * @return the error message resulting from an unsuccessful parse
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /**
     * Parse the inputStream as a Paint Save File Format file. The result of the
     * parse is stored as an ArrayList of Paint command. If the parse was not
     * successful, this.errorMessage is appropriately set, with a useful error
     * message.
     *
     * @param inputStream the open file to parse
     * @param paintModel the paint model to add the commands to
     * @return whether the complete file was successfully parsed
     * @throws IOException
     */
    public boolean parse(BufferedReader inputStream, PaintModel paintModel) throws IOException {
        try {
            this.paintModel = paintModel;
            this.errorMessage = "";

            // During the parse, we will be building one of the
            // following commands. As we parse the file, we modify
            // the appropriate command.
            CircleCommand circleCommand = null;
            RectangleCommand rectangleCommand = null;
            SquiggleCommand squiggleCommand = null;
            PolygonCommand polygonCommand = null;

            int state = 0;
            Matcher m;
            String l;

            this.lineNumber = 0;
            while ((l = inputStream.readLine()) != null) { // loop
                this.lineNumber++;
                System.out.println(lineNumber + " " + l + " " + state);
                switch (state) {
                    case 0:
                        m = pFileStart.matcher(l);

                        if (m.matches()) {
                            state = 1;
                            break;
                        }
                        error("Expected Start of Paint Save File");
                        return false;
                    case 1: // Looking for the start of a new object or end of the
                        // save file
                        m = pFileEnd.matcher(l);
                        if (m.matches()) {
                            state = 99;
                            break;
                        }

                        m = pAnyShape.matcher(l);
                        if (m.matches()) {
                            // Regardless of the shape, it should have a color and a
                            // filled status, thus the state will be set to 2
                            state = 2;

                            // Store which shape is at the lth line in the form of a
                            // boolean system
                            m = pCircleStart.matcher(l);
                            if (m.matches()) {
                                stateReset();
                                isCircle = true;
                                break;
                            }

                            m = pRectangleStart.matcher(l);
                            if (m.matches()) {
                                stateReset();
                                isRectangle = true;
                                break;
                            }

                            m = pSquiggleStart.matcher(l);
                            if (m.matches()) {
                                stateReset();
                                isSquiggle = true;
                                break;
                            }

                            m = pPolygonStart.matcher(l);
                            if (m.matches()) {
                                stateReset();
                                isPolygon = true;
                                break;
                            }

                            break;
                        }

                    case 2:
                        // Color
                        m = pGeneralColor.matcher(l);
                        if (m.matches()) {
                            try {
                                shapeColor = (getColor(l));
                            } catch (Exception e1) {
                                error("Incorrect Color Value(s)");
                                return false;
                            }
                            state = 3;
                            break;
                        } else {
                            error("Incorrect Color Value(s)");
                            return false;
                        }

                    case 3:
                        // Filled
                        m = pGeneralIsFilled.matcher(l);
                        if (m.matches()) {
                            shapeIsFilled = getIsFilled(l);

                            // Will change to the correct state based on the boolean
                            // system
                            if (isCircle) {
                                state = 11;
                                break;
                            } else if (isRectangle) {
                                state = 21;
                                break;
                            } else if (isSquiggle) {
                                state = 31;
                                break;
                            } else if (isPolygon) {
                                state = 41;
                                break;
                            }
                        }
                        error("Incorrect Boolean Value");
                        return false;

                    case 11: // CIRCLE
                        m = pCircleCenter.matcher(l);
                        if (m.matches()) {
                            shapeCenter = getCenter(l);
                            state = 12;
                            break;
                        } else {
                            error("Invalid Circle Center");
                            return false;
                        }

                    case 12:
                        m = pCircleRadius.matcher(l);
                        if (m.matches()) {
                            shapeRadius = getRadius(l);
                            state = 13;
                            break;
                        } else {
                            error("Invalid Circle Radius");
                            return false;
                        }

                    case 13:
                        m = pCircleEnd.matcher(l);
                        if (m.matches()) {
                            circleCommand = new CircleCommand(shapeCenter, shapeRadius);
                            circleCommand.setColor(shapeColor);
                            circleCommand.setFill(shapeIsFilled);
                            paintModel.addCommand(circleCommand);
                            state = 1;
                            break;
                        } else {
                            error("Invalid Circle Ending");
                            return false;
                        }

                    case 21: // RECTANGLE
                        m = pRectangleP1.matcher(l);
                        if (m.matches()) {
                            shapeP1 = getP1(l);
                            state = 22;
                            break;
                        } else {
                            error("Invalid Rectangle P1");
                            return false;
                        }

                    case 22:
                        m = pRectangleP2.matcher(l);
                        if (m.matches()) {
                            shapeP2 = getP2(l);
                            state = 23;
                            break;
                        } else {
                            error("Invalid Rectangle P2");
                            return false;
                        }

                    case 23:
                        m = pRectangleEnd.matcher(l);
                        if (m.matches()) {
                            rectangleCommand = new RectangleCommand(shapeP1, shapeP2);
                            rectangleCommand.setColor(shapeColor);
                            rectangleCommand.setFill(shapeIsFilled);
                            paintModel.addCommand(rectangleCommand);
                            state = 1;
                            break;
                        } else {
                            error("Invalid Rectangle Ending");
                            return false;
                        }

                    case 31: // Squiggle
                        m = pSquigglePointsStart.matcher(l);
                        if (m.matches()) {
                            state = 32;
                            break;
                        } else {
                            error("Invalid Squiggle Point List");
                            return false;
                        }

                    case 32:
                        m = pSquigglePoint.matcher(l);
                        if (m.matches()) {
                            shapePoints.add(getPoint(l));
                            state = 32;
                            break;
                        }
                        m = pSquigglePointsEnd.matcher(l);
                        if (m.matches()) {
                            state = 33;
                            break;
                        } else {
                            error("Invalid Squiggle Point");
                            return false;
                        }

                    case 33:
                        m = pSquiggleEnd.matcher(l);
                        if (m.matches()) {
                            squiggleCommand = new SquiggleCommand();
                            squiggleCommand.setColor(shapeColor);
                            squiggleCommand.setFill(shapeIsFilled);
                            for (Point temp : shapePoints) {
                                squiggleCommand.add(temp);
                            }
                            paintModel.addCommand(squiggleCommand);
                            state = 1;
                            break;
                        } else {
                            error("Invalid Squiggle Ending");
                            return false;
                        }

                    case 41: // Polygon
                        m = pPolygonPointsStart.matcher(l);
                        if (m.matches()) {
                            state = 42;
                            break;
                        } else {
                            error("Invalid Polygon Point List");
                            return false;
                        }

                    case 42:
                        m = pPolygonPoint.matcher(l);
                        if (m.matches()) {
                            shapePoints.add(getPoint(l));
                            state = 42;
                            break;
                        }
                        m = pPolygonPointsEnd.matcher(l);
                        if (m.matches()) {
                            state = 43;
                            break;
                        } else {
                            error("Invalid Polygon Point");
                            return false;
                        }

                    case 43:
                        m = pPolygonEnd.matcher(l);
                        if (m.matches()) {
                            polygonCommand = new PolygonCommand();
                            polygonCommand.setColor(shapeColor);
                            polygonCommand.setFill(shapeIsFilled);
                            for (Point temp : shapePoints) {
                                squiggleCommand.add(temp);
                            }
                            paintModel.addCommand(polygonCommand);
                            state = 1;
                            break;
                        } else {
                            error("Invalid Polygon Ending");
                            return false;
                        }

                    case 99:
                        m = pFileEnd.matcher(l);
                        if (m.matches()) {
                            return true;
                        } else {
                            error("Invalid file end");
                            return false;
                        }

                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void stateReset() {
        isCircle = false;
        isRectangle = false;
        isSquiggle = false;
        isPolygon = false;
        shapeColor = null;
        shapeIsFilled = false;
        shapeRadius = -1;
        shapeCenter = shapeP1 = shapeP2 = null;
        shapePoints.clear();
    }

    private Color getColor(String s) {
        s = s.replaceAll("[^0123456789,]", " ");
        s = s.replaceAll(" ", "");
        s = s.replaceAll(",", " ");
        String[] t = s.split(" ");

        Color tempColor = Color.rgb(Integer.parseInt(t[0]), Integer.parseInt(t[1]), Integer.parseInt(t[2]));
        return tempColor;
    }

    private boolean getIsFilled(String s) {
        if (s.equals("true")) {
            return true;
        } else {
            return false;
        }
    }

    private Point getCenter(String s) {
        s = s.replaceAll("[^0123456789,]", " ");
        s = s.replaceAll(" ", "");
        s = s.replaceAll(",", " ");
        String[] t = s.split(" ");

        Point tempPoint = new Point(Integer.parseInt(t[0]), Integer.parseInt(t[1]));
        System.out.println(tempPoint.toString());
        return tempPoint;
    }

    private int getRadius(String s) {
        s = s.replaceAll("[^0-9.]", " ");
        s = s.replace(" ", "");
        return Integer.parseInt(s);
    }

    private Point getP1(String s) {
        Point tempPoint = getCenter(s);
        String tempX = Integer.toString(tempPoint.getX());
        int oldY = tempPoint.getY();
        int newX = Integer.parseInt(tempX.substring(1));
        tempPoint = new Point(newX, oldY);
        return tempPoint;
    }

    private Point getP2(String s) {
        return getP1(s);
    }

    private Point getPoint(String s) {
        return getCenter(s);
    }
}
