package org.firstinspires.ftc.teamcode.robots.reachRefactor.subsystem;

import com.acmerobotics.dashboard.canvas.Canvas;
import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.robots.reachRefactor.simulation.CRServoSim;
import org.firstinspires.ftc.teamcode.robots.reachRefactor.simulation.DistanceSensorSim;
import org.firstinspires.ftc.teamcode.robots.reachRefactor.simulation.ServoSim;
import org.firstinspires.ftc.teamcode.statemachine.Stage;
import org.firstinspires.ftc.teamcode.statemachine.StateMachine;

import static org.firstinspires.ftc.teamcode.robots.reachRefactor.util.Utils.*;

@Config
public class Gripper implements Subsystem {
    public static int CLOSED = 1300;
    public static int RELEASE = 1700;
    public static int OPEN = 1500;
    public static int PITCH_TRANSFER = 2100;
    public static int PITCH_DOWN = 800;
    public static int PITCH_VERTICAL = 1800;
    public static int FREIGHT_TRIGGER = 50; //mm distance to trigger Lift articulation

    private final Servo pitchServo, servo;
    private final CRServo intakeServo;
    private final DistanceSensor freightSensor;

    // State
    boolean up = true;
    boolean open = true;

    private int targetPos, pitchTargetPos;
    private double intakePower;

    private double freightDistance;

    public Articulation articulation;
    private final Map<Gripper.Articulation, StateMachine> articulationMap;

    public Gripper(HardwareMap hardwareMap, boolean simulated){
        if(simulated) {
            servo = new ServoSim();
            pitchServo = new ServoSim();
            intakeServo = new CRServoSim();
            freightSensor = new DistanceSensorSim(100);
        } else {
            servo = hardwareMap.get(Servo.class, "gripperServo");
            pitchServo = hardwareMap.get(Servo.class, "gripperPitchServo");
            freightSensor = hardwareMap.get(RevColorSensorV3.class, "freightSensor");
            intakeServo = hardwareMap.get(CRServo.class, "intakeServo");
            intakeServo.setDirection(DcMotorSimple.Direction.REVERSE);
        }

        articulation = Gripper.Articulation.MANUAL;

        articulationMap = new HashMap<>();
        articulationMap.put(Gripper.Articulation.SET,set);
        articulationMap.put(Gripper.Articulation.LIFT, lift);
        articulationMap.put(Articulation.TRANSFER, transfer);
    }

    @Override
    public void update(Canvas fieldOverlay) {
        freightDistance = freightSensor.getDistance(DistanceUnit.MM);

        articulate(articulation);
        servo.setPosition(servoNormalize(targetPos));
        pitchServo.setPosition(servoNormalize(pitchTargetPos));
        intakeServo.setPower(intakePower);
    }

    public void stop() {
        articulation = Articulation.MANUAL;
        targetPos = 750;
        pitchTargetPos = 750;
    }

    public enum Articulation {
        MANUAL,
        GRIP,
        TRANSFER,
        SET, //Set for Intaking - can be used as an emergency release
        LIFT //Grip and lift to vertical
    }

    public boolean articulate(Gripper.Articulation articulation) {

        this.articulation = articulation;

            if(articulation.equals(Gripper.Articulation.MANUAL))
                return true;
            else if(articulationMap.get(articulation).execute()) {
                this.articulation = Gripper.Articulation.MANUAL;
                return true;
            }
            return false;
        }

    //Set the gripper for intake - assume this is coming down from the released transfer position
    //Elevation is down and jaws are open wide to just prevent 2 boxes slipping in
    //Do not assume that we want to Set directly out of Transfer - there may be barriers to cross
    private final Stage setStage = new Stage();
    private final StateMachine set = getStateMachine(setStage)
            .addSingleState(() -> setIntakePower(1.0))
            .addSingleState(()->{setTargetPos(CLOSED);}) //close the gripper so it's less likely to catch something
            .addTimedState(.25f, () -> setPitchTargetPos(PITCH_DOWN), () -> {})
            .addTimedState(.5f, ()->{setTargetPos(OPEN);}, () -> {})
            .build();

    //Gripper closes and lifts to the Transfer-ready position
    //Gripper remains closed - Transfer is separate
    private final Stage liftStage = new Stage();
    private final StateMachine lift = getStateMachine(liftStage)
            .addSingleState(() -> setIntakePower(0))
            .addTimedState(() -> .5f, () -> setTargetPos(CLOSED), () -> {})//close the gripper so it's less likely to catch something
            .addTimedState(() -> .5f, () -> setPitchTargetPos(PITCH_VERTICAL), () -> {})
            .build();

    private final Stage transferStage = new Stage();
    private final StateMachine transfer = getStateMachine(transferStage)
            .addTimedState(() -> .1f, () -> setPitchTargetPos(PITCH_TRANSFER), () -> {})//give freight last-second momentum toward the bucket
            .addTimedState(() -> .75f, () -> setTargetPos(RELEASE), () -> {})
            .addTimedState(() -> 0, () -> setTargetPos(CLOSED), () -> {})
            .addTimedState(() -> 0, () -> setPitchTargetPos(PITCH_VERTICAL), () -> {})
            .build();

    //Prepare for intake
    public void set() {
        articulation = Articulation.SET;
    }

    // grip and lift into Transfer position - this might need timing
    public void lift() {
        articulation = Articulation.LIFT;
    }

    @Override
    public Map<String, Object> getTelemetry(boolean debug) {
        Map<String, Object> telemetryMap = new LinkedHashMap<>();

        if(debug) {
            telemetryMap.put("Servo Target Pos", targetPos);
            telemetryMap.put("Pitch Servo Target Pos", pitchTargetPos);
            telemetryMap.put("intake power", intakePower);
            telemetryMap.put("Open", open);
            telemetryMap.put("Up", up);
            telemetryMap.put("Freight Distance", freightDistance);
        }

        return telemetryMap;
    }

    @Override
    public String getTelemetryName() {
        return "Gripper";
    }

    //----------------------------------------------------------------------------------------------
    // Getters And Setters
    //----------------------------------------------------------------------------------------------

    public int getTargetPos() {
        return targetPos;
    }

    private void setTargetPos(int targetPos) {
        this.targetPos = targetPos;
    }

    public void setTargetPosDiag(int targetPos) {
        this.targetPos = targetPos;
    }

    public int getPitchTargetPos() {
        return pitchTargetPos;
    }

    public double getFreightDistance(){return freightDistance;}

    private void setPitchTargetPos(int pitchTargetPos) {
        this.pitchTargetPos = pitchTargetPos;
    }
    public void setPitchTargetPosDiag(int pitchTargetPos) {
        this.pitchTargetPos = pitchTargetPos;
    }
    public void setIntakePower(double intakePower) { this.intakePower = intakePower; }
}
