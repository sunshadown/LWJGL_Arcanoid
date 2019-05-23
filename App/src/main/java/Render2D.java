import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.*;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Render2D extends Model{

    private int filtertype;
    private boolean isFBOtex;
    private boolean isFixedSize = false;
    private int FBOid;
    private Texture texture;
    private Vector2f size;
    private float rotate;

    public Render2D(){
        setFiltertype(1);
        isFBOtex = false;
        Init();
    }

    public Render2D(int filtertype,boolean isFBOtex,int FBOid){
        this.setFiltertype(filtertype);
        this.isFBOtex = isFBOtex;
        this.setFBOid(FBOid);
        Init();
    }

    public Render2D(int filtertype, Vector3f position, Vector2f size, float roate){
        this.position = position;
        this.size = size;
        this.rotate = rotate;
        this.isFixedSize = true;
        this.setFiltertype(filtertype);
        Init();
    }
    @Override
    public void Init() {
        shader = new Shader();
        InitShader();
        InitBuffer();

    }

    @Override
    public void Update(float dt) {

    }

    @Override
    public void Render(Matrix4f view, Matrix4f proj,Matrix4f ortho) {
        glBindVertexArray(VAO);
        shader.bind();

        glUniform1i(shader.getUniform("mySampler"), 0);
        glUniform1i(shader.getUniform("filtertype"), getFiltertype());

        glActiveTexture(GL_TEXTURE0);
        if (isFBOtex)
        {
            glBindTexture(GL_TEXTURE_2D, getFBOid());
        }
        else {
            //TextureManager::Inst()->BindTexture(id);
            texture.bind();
        }

        Matrix4f model = new Matrix4f();
        model.zero();
        model.m00(1);
        model.m11(1);
        model.m22(1);
        model.m33(1);
        if(isFixedSize){
            FloatBuffer modelbuf = BufferUtils.createFloatBuffer(16);
            FloatBuffer orthobuf = BufferUtils.createFloatBuffer(16);
            model.translate(position.x,position.y,0.0f);
            model.translate(size.x *0.5f,0.5f*size.y,0.0f);
            model.rotate((float)Math.toRadians(rotate),0,0,1);
            model.translate(size.x *-0.5f,-0.5f*size.y,0.0f);
            model.scale(size.x,size.y,1.0f);
            model.get(modelbuf);
            ortho.get(orthobuf);
            glUniformMatrix4fv(shader.getUniform("model"),false,modelbuf);
            glUniformMatrix4fv(shader.getUniform("projection"),false,orthobuf);
            glDrawArrays(GL_TRIANGLES,0,6);
        }
        else{
            glDrawArrays(GL_TRIANGLE_STRIP,0,4);
        }

        shader.unbind();
        glBindVertexArray(0);
    }

    private void InitBuffer(){
        VAO = glGenVertexArrays();
        VBO = glGenBuffers();
        EBO = glGenBuffers();

        float []tab = {
                -1.0f,-1.0f,
                1.0f,-1.0f,
                -1.0f,1.0f,
                1.0f,1.0f};
        int []indicies = {0,1,2,3};

        float vertices[] = {
                // Pos      // Tex
                0.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 0.0f,

                0.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 0.0f
        };
        glBindVertexArray(VAO);
        FloatBuffer floatBuffer;
        if(isFixedSize){
            floatBuffer = BufferUtils.createFloatBuffer(vertices.length);
            floatBuffer.put(vertices).flip();
            glBindBuffer(GL_ARRAY_BUFFER,VBO);
            glBufferData(GL_ARRAY_BUFFER,floatBuffer,GL_STATIC_DRAW);
            glEnableVertexAttribArray(shader.getAttrib("vPosition"));
            glVertexAttribPointer(shader.getAttrib("vPosition"),4,GL_FLOAT,false,0,0);

        }else{
            floatBuffer = BufferUtils.createFloatBuffer(tab.length);
            floatBuffer.put(tab).flip();
            glBindBuffer(GL_ARRAY_BUFFER,VBO);
            glBufferData(GL_ARRAY_BUFFER,floatBuffer,GL_STATIC_DRAW);
            glEnableVertexAttribArray(shader.getAttrib("vPosition"));
            glVertexAttribPointer(shader.getAttrib("vPosition"),2,GL_FLOAT,false,0,0);

        }


        IntBuffer intBuffer = BufferUtils.createIntBuffer(indicies.length);
        intBuffer.put(indicies).flip();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,EBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER,intBuffer,GL_STATIC_DRAW);

        glBindVertexArray(0);
    }

    private void InitShader(){
        if(isFixedSize){
            try {
                shader.attachVertexShader(getClass().getResource("/Shaders/renderer2Dfixed.vs").getPath().substring(1));
                shader.attachFragmentShader(getClass().getResource("/Shaders/renderer2Dfixed.fs").getPath().substring(1));
                shader.link();
            }catch(IOException err){
                System.err.println(err);
            }
        }else{
            try {
                shader.attachVertexShader(getClass().getResource("/Shaders/renderer2D.vs").getPath().substring(1));
                shader.attachFragmentShader(getClass().getResource("/Shaders/renderer2D.fs").getPath().substring(1));
                shader.link();
            }catch(IOException err){
                System.err.println(err);
            }
        }
    }

    public void LoadTex(String path){
        texture = Texture.loadTexture(path);
    }

    public int getFBOid() {
        return FBOid;
    }

    public void setFBOid(int FBOid) {
        this.FBOid = FBOid;
    }

    public int getFiltertype() {
        return filtertype;
    }

    public void setFiltertype(int filtertype) {
        this.filtertype = filtertype;
    }
}
