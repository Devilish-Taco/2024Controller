package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

@Autonomous(name="Vanilla", group="Robot")

public class RobotAutoDriveByTime_Linear extends LinearOpMode {
    // Initialize all variables for the program
    // Hardware variables
    private IMU imu = null;
    private DcMotor leftFrontDrive = null;
    private DcMotor rightFrontDrive = null;
    private DcMotor leftBackDrive = null;
    private DcMotor rightBackDrive = null;
    // Software variables
    private final ElapsedTime     runtime = new ElapsedTime();
    static final double     DEFAULT_SPEED = 0.6;

    @Override
    public void runOpMode() {
        // Define all the hardware
        leftFrontDrive = hardwareMap.get(DcMotor.class, "left_front_drive");
        leftBackDrive = hardwareMap.get(DcMotor.class, "left_back_drive");
        rightFrontDrive = hardwareMap.get(DcMotor.class, "right_front_drive");
        rightBackDrive = hardwareMap.get(DcMotor.class, "right_back_drive");
        leftFrontDrive.setDirection(DcMotor.Direction.REVERSE);
        leftBackDrive.setDirection(DcMotor.Direction.REVERSE);
        rightFrontDrive.setDirection(DcMotor.Direction.FORWARD);
        rightBackDrive.setDirection(DcMotor.Direction.FORWARD);

        RevHubOrientationOnRobot.LogoFacingDirection logoDirection = RevHubOrientationOnRobot.LogoFacingDirection.UP;
        RevHubOrientationOnRobot.UsbFacingDirection  usbDirection  = RevHubOrientationOnRobot.UsbFacingDirection.FORWARD;
        RevHubOrientationOnRobot orientationOnRobot = new RevHubOrientationOnRobot(logoDirection, usbDirection);

        imu = hardwareMap.get(IMU.class, "imu");
        imu.initialize(new IMU.Parameters(orientationOnRobot));
        imu.resetYaw();

        // Send telemetry message to signify robot waiting;
        telemetry.addData("Status", "Ready to run");
        telemetry.update();

        telemetry.addData("Current Yaw", "%.0f", getHeading());
        telemetry.update();
        sleep(5000);

        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        // Start of autonomous program
        /*
       turnLeft(2);
       turnRight(0.3);
       moveBackward(0.5);
       moveForward(0.5);
       moveBackward(0.5);
       moveForward(0.5);
       turnLeft(0.7);
       turnRight(0.7);
       for(int i=0; i < 2; i++)
        {
            turnLeft(2);
            moveForward(0.3);
            moveBackward(0.3);
            turnRight(2);
        }
       */
       turnLeftToHeading(45, .7);
       sleep(2000);
       turnLeftToHeading(90, .7);
       sleep(2000);
       turnRightToHeading(45, .7);
       sleep(2000);
       turnRightToHeading(0, .7);
       sleep(5000);

        // End of autonomous program

        telemetry.addData("Path", "Complete");
        telemetry.addData("Current Yaw", "%.0f", getHeading());
        telemetry.update();
        sleep(5000);
    }

    private void moveForward(double secondsToDrive) {
        moveForward(secondsToDrive, DEFAULT_SPEED);
    }

    private void moveForward(double secondsToDrive, double speedToDrive) {
        leftFrontDrive.setPower(speedToDrive);
        rightFrontDrive.setPower(speedToDrive);
        leftBackDrive.setPower(speedToDrive);
        rightBackDrive.setPower(speedToDrive);
        runtime.reset();
        while (opModeIsActive() && (runtime.seconds() < secondsToDrive)) {
            telemetry.addData("Move forward: %4.1f S Elapsed", runtime.seconds());
            telemetry.update();
        }
        stopMoving();
    }

    private void moveBackward(double secondsToDrive) {
        moveBackward(secondsToDrive, DEFAULT_SPEED);
    }

    private void moveBackward(double secondsToDrive, double speedToDrive) {
        leftFrontDrive.setPower(-speedToDrive);
        rightFrontDrive.setPower(-speedToDrive);
        leftBackDrive.setPower(-speedToDrive);
        rightBackDrive.setPower(-speedToDrive);
        runtime.reset();
        while (opModeIsActive() && (runtime.seconds() < secondsToDrive)) {
            telemetry.addData("Move backward: %4.1f S Elapsed", runtime.seconds());
            telemetry.update();
        }
        stopMoving();
    }

    private void turnRight(double secondsToDrive) {
        turnRight(secondsToDrive, DEFAULT_SPEED);
    }

    private void turnRight(double secondsToDrive, double speedToDrive) {
        leftFrontDrive.setPower(speedToDrive);
        rightFrontDrive.setPower(-speedToDrive);
        leftBackDrive.setPower(speedToDrive);
        rightBackDrive.setPower(-speedToDrive);
        runtime.reset();
        while (opModeIsActive() && (runtime.seconds() < secondsToDrive)) {
            telemetry.addData("Turn right: %4.1f S Elapsed", runtime.seconds());
            telemetry.update();
        }
        stopMoving();
    }

    private void turnLeft(double secondsToDrive) {
        turnLeft(secondsToDrive, DEFAULT_SPEED);
    }

    private void turnLeft(double secondsToDrive, double speedToDrive) {
        leftFrontDrive.setPower(-speedToDrive);
        rightFrontDrive.setPower(speedToDrive);
        leftBackDrive.setPower(-speedToDrive);
        rightBackDrive.setPower(speedToDrive);
        runtime.reset();
        while (opModeIsActive() && (runtime.seconds() < secondsToDrive)) {
            telemetry.addData("Turn left: %4.1f S Elapsed", runtime.seconds());
            telemetry.update();
        }
        stopMoving();
    }

    private void stopMoving() {
        leftFrontDrive.setPower(0);
        rightFrontDrive.setPower(0);
        leftBackDrive.setPower(0);
        rightBackDrive.setPower(0);
    }

    public double getHeading() {
        YawPitchRollAngles orientation = imu.getRobotYawPitchRollAngles();
        return orientation.getYaw(AngleUnit.DEGREES);
    }
    private void turnLeftToHeading(double targetYaw, double speedToDrive) {
        leftFrontDrive.setPower(-speedToDrive);
        rightFrontDrive.setPower(speedToDrive);
        leftBackDrive.setPower(-speedToDrive);
        rightBackDrive.setPower(speedToDrive);

        while (getHeading() < targetYaw) {
            telemetry.addData("Current Yaw", "%.0f", getHeading());
            telemetry.update();
        }
        stopMoving();
    }

    private void turnRightToHeading(double targetYaw, double speedToDrive) {
        leftFrontDrive.setPower(speedToDrive);
        rightFrontDrive.setPower(-speedToDrive);
        leftBackDrive.setPower(speedToDrive);
        rightBackDrive.setPower(-speedToDrive);

        while (getHeading() > targetYaw) {
            telemetry.addData("Current Yaw", "%.0f", getHeading());
            telemetry.update();
        }
        stopMoving();
    }
}