package com.jayfella.jme.vehicle.skid;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * A single continuous skid mark, split into sections and rendered using a
 * single Mesh.
 */
public class SkidMarkManager {
    // *************************************************************************
    // classes and enums

    /*
     * A single section within a continuous skid mark.
     */
    class MarkSection {

        public float Intensity;
        public int LastIndex;
        public Vector3f Normal = new Vector3f(); // TODO use FloatBuffers
        public Vector3f Pos = new Vector3f();
        public Vector3f Posl = new Vector3f();
        public Vector3f Posr = new Vector3f();
        public Vector4f Tangent = new Vector4f();
    }
    // *************************************************************************
    // constants and loggers

    /**
     * height of the skid above the pavement (in meters)
     */
    final private float GROUND_OFFSET = 0.02f;
    /**
     * minimum distance travelled before starting a new section (in meters),
     * bigger means better performance but less smooth
     */
    final private float MIN_DISTANCE = 0.5f;
    final private float MIN_SQR_DISTANCE = MIN_DISTANCE * MIN_DISTANCE;
    // *************************************************************************
    // fields

    private boolean haveSetBounds;
    /**
     * true if an update() is needed (a section has been added since the
     * previous update)
     */
    private boolean meshUpdated;
    /**
     * array used to initialize color buffer in the Mesh
     */
    final private ColorRGBA[] colors;
    /**
     * width of this skid mark (in meters), should match the width of the tire
     */
    final private float MARK_WIDTH;
    /**
     * Geometry used to visualize this skid mark
     */
    private Geometry geometry;
    /**
     * cyclic index into the array of sections
     */
    private int markIndex;
    /**
     * maximum number of sections in this skid mark
     */
    final private int MAX_MARKS;
    /**
     * array used to initialize the index buffer of the Mesh
     */
    final private int[] triangles;
    /**
     * circular buffer of sections
     */
    final private MarkSection[] skidmarks;
    /**
     * Material used to visualize this skid mark
     */
    final private Material skidmarksMaterial;
    /**
     * Mesh used to visualize this skid mark
     */
    final private Mesh marksMesh;
    /**
     * arrays used to initialize mesh buffers
     */
    final private Vector3f[] normals;
    final private Vector4f[] tangents;
    final private Vector2f[] uvs;
    final private Vector3f[] vertices;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a continuous skid mark with the specified width.
     *
     * @param assetManager
     * @param maxSkidDistance
     * @param tireWidth
     */
    public SkidMarkManager(AssetManager assetManager, int maxSkidDistance, float tireWidth) {
        /*
         * Generate a fixed array of sections.
         */
        this.MAX_MARKS = maxSkidDistance;
        this.MARK_WIDTH = tireWidth;

        skidmarks = new MarkSection[MAX_MARKS];
        for (int i = 0; i < MAX_MARKS; i++) {
            skidmarks[i] = new MarkSection();
        }

        marksMesh = new Mesh();
        marksMesh.setDynamic();

        vertices = new Vector3f[MAX_MARKS * 4];
        normals = new Vector3f[MAX_MARKS * 4];
        tangents = new Vector4f[MAX_MARKS * 4];
        colors = new ColorRGBA[MAX_MARKS * 4];
        uvs = new Vector2f[MAX_MARKS * 4];
        triangles = new int[MAX_MARKS * 6];

        this.skidmarksMaterial = assetManager.loadMaterial("Materials/Vehicles/SkidMark.j3m");
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Add a section to this skid mark. The alpha component of the vertex color
     * indicates the skid intensity.
     *
     * @param pos the final location for the new section (in world coordinates,
     * not null, unaffected)
     * @param normal the final normal for the new section (in world coordinates,
     * not null, unaffected)
     * @param intensity the final intensity for the new section
     * @param lastIndex the index of the previous section (&ge;0, &lt;MAX_MARKS)
     * @return the index of the new section (&ge;0, &lt;MAX_MARKS)
     */
    public int addSkidMark(Vector3f pos, Vector3f normal, float intensity, int lastIndex) {
        if (intensity > 1) intensity = 1.0f;
        else if (intensity < 0) return -1;

        if (lastIndex > 0) {
            float sqrDistance = pos.subtract(skidmarks[lastIndex].Pos).length(); // TODO oops
            if (sqrDistance < MIN_SQR_DISTANCE) return lastIndex;
        }

        MarkSection curSection = skidmarks[markIndex];

        curSection.Pos = pos.add(normal.mult(GROUND_OFFSET));
        curSection.Normal = normal;
        curSection.Intensity = intensity;
        curSection.LastIndex = lastIndex;

        if (lastIndex != -1) {
            MarkSection lastSection = skidmarks[lastIndex];
            Vector3f dir = curSection.Pos.subtract(lastSection.Pos);
            Vector3f xDir = dir.cross(normal).normalizeLocal();

            curSection.Posl = curSection.Pos.add(xDir.mult(MARK_WIDTH * 0.5f));
            curSection.Posr = curSection.Pos.subtract(xDir.mult(MARK_WIDTH * 0.5f));
            curSection.Tangent = new Vector4f(xDir.x, xDir.y, xDir.z, 1);

            if (lastSection.LastIndex == -1) {
                lastSection.Tangent = curSection.Tangent;
                lastSection.Posl = curSection.Pos.add(xDir.mult(MARK_WIDTH * 0.5f));
                lastSection.Posr = curSection.Pos.subtract(xDir.mult(MARK_WIDTH * 0.5f));
            }
        }

        updateSkidMarksMesh();

        int curIndex = markIndex;
        // Update circular index
        markIndex = ++markIndex % MAX_MARKS;

        return curIndex;
    }

    /**
     * Access the Geometry used to render the marks.
     *
     * @return the pre-existing instance
     */
    public Geometry getGeometry() {
        return geometry;
    }
    // *************************************************************************
    // new protected methods

    protected void update() {
        if (!meshUpdated) return;
        meshUpdated = false;

        FloatBuffer pb = BufferUtils.createFloatBuffer(vertices);
        marksMesh.setBuffer(VertexBuffer.Type.Position, 3, pb);

        FloatBuffer nb = BufferUtils.createFloatBuffer(normals);
        marksMesh.setBuffer(VertexBuffer.Type.Normal, 3, nb);

        IntBuffer ib = BufferUtils.createIntBuffer(triangles);
        marksMesh.setBuffer(VertexBuffer.Type.Index, 3, ib);

        FloatBuffer ub = BufferUtils.createFloatBuffer(uvs);
        marksMesh.setBuffer(VertexBuffer.Type.TexCoord, 2, ub);

        FloatBuffer tb = BufferUtils.createFloatBuffer(tangents);
        marksMesh.setBuffer(VertexBuffer.Type.Tangent, 4, tb);

        FloatBuffer cb = BufferUtils.createFloatBuffer(colors);
        marksMesh.setBuffer(VertexBuffer.Type.Color, 4, cb);

        marksMesh.updateBound();

        if (this.geometry == null) {
            this.geometry = new Geometry("SkidMark", marksMesh);
            this.geometry.setMaterial(skidmarksMaterial);
            this.geometry.setQueueBucket(RenderQueue.Bucket.Transparent);
        }

        geometry.updateModelBound();

        if (!haveSetBounds) {
            // Could use RecalculateBounds here each frame instead, but it uses about 0.1-0.2ms each time
            // Save time by just making the mesh bounds huge, so the skidmarks will always draw
            // Not sure why I only need to do this once, yet can't do it in Start (it resets to zero)
            // marksMesh.bounds = new Bounds(new Vector3(0, 0, 0), new Vector3(10000, 10000, 10000));
            // haveSetBounds = true;
            //marksMesh.updateBound();
        }
    }
    // *************************************************************************
    // private methods

    /**
     * Add 4 mesh vertices (2 triangles) in order to connect the current
     * MarkSection to the previous one.
     */
    private void updateSkidMarksMesh() {
        MarkSection curr = skidmarks[markIndex];

        // Nothing to connect to yet
        if (curr.LastIndex == -1) return;

        MarkSection last = skidmarks[curr.LastIndex];
        vertices[markIndex * 4 + 0] = last.Posl;
        vertices[markIndex * 4 + 1] = last.Posr;
        vertices[markIndex * 4 + 2] = curr.Posl;
        vertices[markIndex * 4 + 3] = curr.Posr;

        normals[markIndex * 4 + 0] = last.Normal;
        normals[markIndex * 4 + 1] = last.Normal;
        normals[markIndex * 4 + 2] = curr.Normal;
        normals[markIndex * 4 + 3] = curr.Normal;

        tangents[markIndex * 4 + 0] = last.Tangent;
        tangents[markIndex * 4 + 1] = last.Tangent;
        tangents[markIndex * 4 + 2] = curr.Tangent;
        tangents[markIndex * 4 + 3] = curr.Tangent;

        // dirt
        float r = 43 / 255f;
        float g = 29 / 255f;
        float b = 14 / 255f;

        colors[markIndex * 4 + 0] = new ColorRGBA(r, g, b, last.Intensity);
        colors[markIndex * 4 + 1] = new ColorRGBA(r, g, b, last.Intensity);
        colors[markIndex * 4 + 2] = new ColorRGBA(r, g, b, curr.Intensity);
        colors[markIndex * 4 + 3] = new ColorRGBA(r, g, b, curr.Intensity);

        uvs[markIndex * 4 + 0] = new Vector2f(0, 0);
        uvs[markIndex * 4 + 1] = new Vector2f(1, 0);
        uvs[markIndex * 4 + 2] = new Vector2f(0, 1);
        uvs[markIndex * 4 + 3] = new Vector2f(1, 1);

        triangles[markIndex * 6 + 0] = markIndex * 4 + 0;
        triangles[markIndex * 6 + 2] = markIndex * 4 + 1;
        triangles[markIndex * 6 + 1] = markIndex * 4 + 2;

        triangles[markIndex * 6 + 3] = markIndex * 4 + 2;
        triangles[markIndex * 6 + 5] = markIndex * 4 + 1;
        triangles[markIndex * 6 + 4] = markIndex * 4 + 3;

        meshUpdated = true;
    }
}
