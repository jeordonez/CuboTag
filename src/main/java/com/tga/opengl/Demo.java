package com.tga.opengl;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Demo {

    // The window handle
    private long window;
    int VAO, VBO, VBO2, VBO3, EBO;
    float angle = 0.0f;

    ShaderProgram shaderProgram;                                                                              
    Matrix4f projection = new Matrix4f();
    Matrix4f view = new Matrix4f(); 
    Matrix4f model = new Matrix4f(); 
    int textureID;
     
    //Metodo para leer .glsl
    public String leerFile(String p) throws FileNotFoundException, IOException{
        FileReader fileReader = new FileReader(p);
        String fileContents = "";
        int i ;
        while((i =  fileReader.read())!=-1){
            char ch = (char)i;
            fileContents = fileContents + ch; 
        }
        return fileContents;
    }

    //Metodo carga textura
    public int cargaTex(String path, int textureID) throws FileNotFoundException, IOException{
            InputStream in = new FileInputStream(path);
            PNGDecoder decoder = new PNGDecoder(in);
            ByteBuffer buf = ByteBuffer.allocateDirect(4*decoder.getWidth()*decoder.getHeight());
            decoder.decode(buf, decoder.getWidth()*4, Format.RGBA);
            buf.flip();
            textureID = glGenTextures();
            System.out.println(textureID);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, textureID); 
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glGenerateMipmap(GL_TEXTURE_2D); 
            return textureID;
        }    
        
    public void run() throws Exception {
        init();
        
        float[] positions = new float[]{
	//Cara z = 1
	-1.0f,	-1.0f,	 1.0f, //0
	 1.0f,	-1.0f,	 1.0f, //1
	-1.0f,	 1.0f,	 1.0f, //2
	 1.0f,	 1.0f,	 1.0f, //3

	//Cara z = -1		   
	-1.0f,	-1.0f,	-1.0f, //4
	 1.0f,	-1.0f,	-1.0f, //5
	-1.0f,	 1.0f,	-1.0f, //6
	 1.0f,	 1.0f,	-1.0f, //7

	//Cara x = 1		   
	1.0f,	-1.0f,	-1.0f, //8
	1.0f,	-1.0f,	 1.0f, //9
	1.0f,	 1.0f,	-1.0f, //10
	1.0f,	 1.0f,	 1.0f, //11

	//Cara x = -1		   
	-1.0f,	-1.0f,	-1.0f, //12
	-1.0f,	-1.0f,	 1.0f, //13
	-1.0f,	 1.0f,	-1.0f, //14
	-1.0f,	 1.0f,	 1.0f, //15

	//Cara y = 1		   
	-1.0f,	 1.0f,	-1.0f, //16
	-1.0f,	 1.0f,	 1.0f, //17
	 1.0f,	 1.0f,	-1.0f, //18
	 1.0f,	 1.0f,	 1.0f, //19

	//Cara y = -1		   
	-1.0f,	-1.0f,	-1.0f, //20
	-1.0f,	-1.0f,	 1.0f, //21
	 1.0f,	-1.0f,	-1.0f, //22
	 1.0f,	-1.0f,	 1.0f  //23
        };


        float[] normals = new float[]{
	//Cara z = 1
	0.0f,	0.0f,	 1.0f, 
	0.0f,	0.0f,	 1.0f, 
	0.0f,	0.0f,	 1.0f, 
	0.0f,	0.0f,	 1.0f, 

	//Cara z = -1		   
	0.0f,	0.0f,	-1.0f, 
	0.0f,	0.0f,	-1.0f, 
	0.0f,	0.0f,	-1.0f, 
	0.0f,	0.0f,	-1.0f, 

	//Cara x = 1		   
	1.0f,	0.0f,	 0.0f, 
	1.0f,	0.0f,	 0.0f, 
	1.0f,	0.0f,	 0.0f, 
	1.0f,	0.0f,	 0.0f, 

	//Cara x = -1		   
	-1.0f,	0.0f,	 0.0f, 
	-1.0f,	0.0f,	 0.0f, 
	-1.0f,	0.0f,	 0.0f, 
	-1.0f,	0.0f,	 0.0f, 

	//Cara y = 1		   
	0.0f,	1.0f,	0.0f, 
	0.0f,	1.0f,	0.0f, 
	0.0f,	1.0f,	0.0f, 
	0.0f,	1.0f,	0.0f, 

	//Cara y = -1		   
	0.0f,	-1.0f,	0.0f, 
	0.0f,	-1.0f,	0.0f, 
	0.0f,	-1.0f,	0.0f, 
	0.0f,	-1.0f,	0.0f 
        };
        float[] texCoords = new float[]{
	//Cara z = 1
	 0.0f, 0.0f,
	 1.0f, 0.0f,
	 0.0f, 1.0f,
	 1.0f, 1.0f,

	//Cara z = -1
	0.0f, 1.0f, 
	1.0f, 1.0f, 
	0.0f, 0.0f, 
	1.0f, 0.0f, 

	//Cara x = 1	
	0.0f,	1.0f,
	1.0f,	1.0f,
	0.0f,	0.0f,
	1.0f,	0.0f,

	//Cara x = -1
	0.0f,	0.0f,
	1.0f,	0.0f,
	0.0f,	1.0f,
	1.0f,	1.0f,

	//Cara y = 1	
	0.0f, 1.0f,
	0.0f, 0.0f,
	1.0f, 1.0f,
	1.0f, 0.0f,

	//Cara y = -1
	0.0f, 0.0f,
	0.0f, 1.0f,
	1.0f, 0.0f,
	1.0f, 1.0f
        };
        //Indexado
        int [] indices = new int[] {
	//Cara z = 1
	0,1,2,			1,3,2,
	//Cara z = -1
	4,6,5,			5,6,7,
	//Cara x = 1
	8,10,9,			9,10,11,
	//Cara x = -1
	12,13,14,		13,15,14,
	//Cara y = 1
	16,17,18,		17,19,18,
	//Cara y = -1
	20,22,21,		21,22,23
        };

        FloatBuffer positionBuffer = null;
        FloatBuffer normalBuffer = null;
        FloatBuffer texCoordBuffer = null;
        IntBuffer indicesBuffer = null;
        try {
        positionBuffer = MemoryUtil.memAllocFloat(positions.length);
        positionBuffer.put(positions).flip();
        
        normalBuffer = MemoryUtil.memAllocFloat(normals.length);
        normalBuffer.put(normals).flip();
        
        texCoordBuffer = MemoryUtil.memAllocFloat(texCoords.length);
        texCoordBuffer.put(texCoords).flip();
        
        indicesBuffer = MemoryUtil.memAllocInt(indices.length);
        indicesBuffer.put(indices).flip();
        
        
        VAO = glGenVertexArrays(); // Create VertexArrayObject
        VBO = glGenBuffers(); // Create VertexBufferObject
        VBO2 = glGenBuffers();
        VBO3 = glGenBuffers();
        EBO = glGenBuffers();
        
        glBindVertexArray(VAO); // Bind current VAO
        
        //position attribute
        glBindBuffer(GL_ARRAY_BUFFER, VBO); // Bind Vertex VAO
        glBufferData(GL_ARRAY_BUFFER, positionBuffer, GL_STATIC_DRAW); // Assign buffer to VBO
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0); // Definition of position attribute in buffer
        glEnableVertexAttribArray(0); // Active attribute 0 on VAO
        
        //normal attribute
        glBindBuffer(GL_ARRAY_BUFFER, VBO2); // Bind Vertex VAO
        glBufferData(GL_ARRAY_BUFFER, normalBuffer, GL_STATIC_DRAW); // Assign buffer to VBO
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0); // Definition of position attribute in buffer
        glEnableVertexAttribArray(1); // Active attribute 0 on VAO
        
        //textCoord attribute
        glBindBuffer(GL_ARRAY_BUFFER, VBO3); // Bind Vertex VAO
        glBufferData(GL_ARRAY_BUFFER, texCoordBuffer, GL_STATIC_DRAW); // Assign buffer to VBO
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0); // Definition of position attribute in buffer
        glEnableVertexAttribArray(2); // Active attribute 0 on VAO
        
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
        
        // Unbind VBO (0 is equal to null VBO)
        glBindBuffer(GL_ARRAY_BUFFER, 0); 
        glBindVertexArray(0); // Unbind VAO (0 is equal to null VAO)
        
        } finally {
            if (positionBuffer != null) {
                MemoryUtil.memFree(positionBuffer); // Destroy auxiliar buffer.
            }
            if (positionBuffer != null) {
                MemoryUtil.memFree(normalBuffer); // Destroy auxiliar buffer.
            }
            if (positionBuffer != null) {
                MemoryUtil.memFree(texCoordBuffer); // Destroy auxiliar buffer.
            }
            if (positionBuffer != null) {
                MemoryUtil.memFree(indicesBuffer); // Destroy auxiliar buffer.
            }
        }
        shaderProgram = new ShaderProgram();
        
//      shaderProgram.createVertexShader(leerFile("vertCubo.glsl"));
//	shaderProgram.createFragmentShader(leerFile("fragCubo.glsl"));
        shaderProgram.createVertexShader(leerFile("E:\\Descargas\\Cubo\\revisarcubo\\src\\main\\java\\com\\tga\\opengl\\vertCubo.glsl"));
	shaderProgram.createFragmentShader(leerFile("E:\\Descargas\\Cubo\\revisarcubo\\src\\main\\java\\com\\tga\\opengl\\fragCubo.glsl"));

	shaderProgram.link();
        shaderProgram.createUniform("projection");
        shaderProgram.createUniform("view");
        shaderProgram.createUniform("model");
        shaderProgram.createUniform("viewPos");
        shaderProgram.createUniform("lightColor");
        shaderProgram.createUniform("lightPos");
        shaderProgram.createUniform("objectColor");

        projection.perspective( (float) Math.toRadians(60.0f), 600.0f/600.0f, 0.001f, 1000.0f);
        view.setTranslation(new Vector3f(0.0f, 0.0f, -4.0f));             
        model.identity();//.translate(new Vector3f(0.0f, 1.0f, 0.0f)).rotate(angle, new Vector3f(1.0f, 0.0f, 0.0f));
       
        textureID = cargaTex("E:\\Master\\TAG\\OpenGLTemplate\\Cubo\\src\\main\\java\\com\\tga\\opengl\\metal.png", textureID);
        //textureID = cargaTex("metal.png", textureID);

        loop();

        // Libera las devoluciones de llamada de la ventana y destruye la ventana
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() throws Exception {
        
        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        // Configure GLFW
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);

        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Create the window
        window = glfwCreateWindow(600, 600, "TAG", NULL, NULL);
        if (window == NULL) {
            glfwTerminate();
            throw new RuntimeException("Failed to create the GLFW window");
        }
        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
            }
            if (key == GLFW_KEY_Q) {
                Vector3f translation = new Vector3f();
                view.getTranslation(translation);
                translation.z += 0.1f;
                view = view.setTranslation(translation);
                System.out.println(translation.z);
            }
            if (key == GLFW_KEY_E) {
                Vector3f translation = new Vector3f();
                view.getTranslation(translation);
                translation.z -= 0.1f;
                view = view.setTranslation(translation);
                System.out.println(translation.z);
            }
            if (key == GLFW_KEY_A) {
                Vector3f translation = new Vector3f();
                view.getTranslation(translation);
                translation.x -= 0.1f;
                view = view.setTranslation(translation);
                System.out.println(translation.x);
            }
            if (key == GLFW_KEY_D) {
                Vector3f translation = new Vector3f();
                view.getTranslation(translation);
                translation.x += 0.1f;
                view = view.setTranslation(translation);
                System.out.println(translation.x);
            }
            if (key == GLFW_KEY_S) {
                Vector3f translation = new Vector3f();
                view.getTranslation(translation);
                translation.y -= 0.1f;
                view = view.setTranslation(translation);
                System.out.println(translation.y);
            }
            if (key == GLFW_KEY_W) {
                Vector3f translation = new Vector3f();
                view.getTranslation(translation);
                translation.y += 0.1f;
                view = view.setTranslation(translation);
                System.out.println(translation.y);
            }
        });

        // Obtener la pila de hilos y empujar un nuevo marco
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Obtener el tamaño de ventana pasado a glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();
    }

    private void loop() throws Exception {
        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        glEnable(GL_DEPTH_TEST);
        //glEnable(GL_BLEND);
        
        while (!glfwWindowShouldClose(window)) {

            glfwPollEvents();
            glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glBindVertexArray(VAO);
            
            shaderProgram.bind(); 
            shaderProgram.setUniform("model", model);
            shaderProgram.setUniform("projection", projection);
            shaderProgram.setUniform("view", view);
            shaderProgram.setUniform("viewPos", 1.2f, 2.0f, 2.0f);
            shaderProgram.setUniform("lightPos", 1.2f, 2.0f, 2.0f);
            shaderProgram.setUniform("lightColor", 1.0f,1.0f,1.0f);
            shaderProgram.setUniform("objectColor", 1.0f,1.0f,1.0f);
            
            //glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            // render
            // ------
 
            //activa la textura     
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, textureID);
            System.out.println(textureID);
 
            //angle += glfwGetTime();
            angle += 1;
            model.identity().translate(new Vector3f(0.0f, 0.0f, 0.0f)).rotate((float)Math.toRadians(angle), new Vector3f(0.0f, 1.0f, 0.0f));

            //glDrawArrays(GL_TRIANGLES, 0, 3);
            glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_INT, 0); // 12*3 los índices comienzan en 0 -> 12 triángulos -> 6 cuadrados
            // glBindVertexArray(0); // no need to unbind it every time
            glfwSwapBuffers(window); // swap the color buffers
        }
    }

    public static void main(String[] args) throws Exception {
        new Demo().run();
    }
}