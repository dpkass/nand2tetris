@0
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
AM=M-1
D=M
@LCL
A=M
M=D
(LOOP_START)
@ARG
A=M
D=M
@SP
A=M
M=D
@SP
M=M+1
@LCL
A=M
D=M
@SP
A=M
M=D
@SP
M=M+1
@SP
AM=M-1
D=M
A=A-1
M=D+M
@SP
AM=M-1
D=M
@LCL
A=M
M=D
@ARG
A=M
D=M
@SP
A=M
M=D
@SP
M=M+1
@1
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
AM=M-1
D=M
A=A-1
M=M-D
@SP
AM=M-1
D=M
@ARG
A=M
M=D
@ARG
A=M
D=M
@SP
A=M
M=D
@SP
M=M+1
@SP
AM=M-1
D=M
@LOOP_START
D;JNE
@LCL
A=M
D=M
@SP
A=M
M=D
@SP
M=M+1
