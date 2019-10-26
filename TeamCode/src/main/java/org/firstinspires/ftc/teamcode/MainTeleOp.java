package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp

public class MainTeleOp extends BaseRobot {

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void start() {
        super.start();
    }
    @Override
    public void loop() {
        super.loop();
        //drive train
        tankanum_drive(gamepad1.right_stick_y, gamepad1.left_stick_y, gamepad1.right_stick_x);

        //arm
        if (gamepad1.dpad_up) {
            arm1 (1);
        } else if (gamepad1.dpad_down) {
            arm1 (-1);
        } else {
            arm1(0);
        }
        if (gamepad1.a) {
            arm2 (1);
        } else if (gamepad1.y) {
            arm2 (-1);
        } else {
            arm2(0);
        }

        //base mover
        if(gamepad1.b) {
            base_mover(1);
        } else if (gamepad1.x) {
            base_mover(-1);
        } else {
            base_mover(0);
        }

        //claw
        if (gamepad1.left_bumper) {
            set_claw_servo_1(ConstantVariables.K_CLAW_SERVO_1_CLOSED);
            set_claw_servo_2(ConstantVariables.K_CLAW_SERVO_2_CLOSED);
        } else if (gamepad1.right_bumper) {
            set_claw_servo_1(ConstantVariables.K_CLAW_SERVO_1_OPEN);
            set_claw_servo_2(ConstantVariables.K_CLAW_SERVO_2_OPEN);
        }
    }
}
