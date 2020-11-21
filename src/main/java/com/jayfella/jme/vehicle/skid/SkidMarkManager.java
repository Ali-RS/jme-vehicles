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
 * A single continuous skid mark, composed of straight sections and rendered
 * using a single Geometry. TODO de-publicize
 */
public class SkidMarkManager {
    // *************************************************************************
    // classes and enums

    /*
     * A single straight section within a skid mark.
     */
    private class MarkSection {

        /**
         * final intensity of the section (&ge;0, &lt;1)
         */
        float intensity;
        /**
         * index of the previous section (&ge;0) or -1 for the first section
         */
        int prevIndex;
        /**
         * final normal (a unit vector in world coordinates)
         */
        Vector3f normal = new Vector3f();
        /**
         * final center location (in world coordinates)
         */
        Vector3f position = new Vector3f();
        /**
         * final location of the left edge (in world coordinates)
         */
        Vector3f positionLeft = new Vector3f();
        /**
         * final location of the right edge (in world coordinates)
         */
        Vector3f positionRight = new Vector3f();
        /**
         * final tangent
         */
        Vector4f tangent = new Vector4f();
    }
    // *************************************************************************
    // constants and loggers

    /**
     * height of the skid above the pavement (in meters) TODO randomize
     */
    final private static float height = 0.02f;
    /**
     * minimum distance travelled before starting a new section (in meters),
     * bigger means better performance but less smooth
     */
    final private static float minLength = 0.25f;
    final private static float minLengthSquared = minLength * minLength;
    // *************************************************************************
    // fields

    /**
     * true if an update() is needed (a section has been added since the
     * previous update)
     */
    private boolean needsUpdate;
    /**
     * array used to initialize color buffer in the Mesh
     */
    final private ColorRGBA[] colors;
    /**
     * width of this skid mark (in meters), should match the width of the tire
     */
    final private float width;
    /**
     * Geometry used to visualize this skid mark
     */
    private Geometry geometry;
    /**
     * maximum number of sections in this skid mark
     */
    final private int maxSections;
    /**
     * cyclic index into the array of sections
     */
    private int sectionIndex;
    /**
     * array used to initialize the index buffer of the Mesh
     */
    final private int[] indices;
    /**
     * circular buffer of sections TODO use an ArrayList
     */
    final private MarkSection[] sections;
    /**
     * Material used to visualize this skid mark
     */
    final private Material material;
    /**
     * dynamic Mesh used to visualize this skid mark
     */
    final private Mesh mesh;
    /**
     * arrays used to initialize mesh buffers
     */
    final private Vector3f[] normals;
    final private Vector3f[] positions;
    final private Vector4f[] tangents;
    final private Vector2f[] uvs;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a continuous skid mark with the specified width. TODO
     * de-publicize
     *
     * @param assetManager for loading assets (not null)
     * @param maxSkidDistance the desired maximum number of sections in this
     * skid mark (&gt;0)
     * @param tireWidth the desired width of this skid mark (in meters, &gt;0)
     */
    public SkidMarkManager(AssetManager assetManager, int maxSkidDistance,
            float tireWidth) {
        /*
         * Generate a fixed array of sections.
         */
        maxSections = maxSkidDistance;
        width = tireWidth;

        sections = new MarkSection[maxSections];
        for (int i = 0; i < maxSections; i++) {
            sections[i] = new MarkSection();
        }

        mesh = new Mesh();
        mesh.setDynamic();

        positions = new Vector3f[maxSections * 4];
        normals = new Vector3f[maxSections * 4];
        tangents = new Vector4f[maxSections * 4];
        colors = new ColorRGBA[maxSections * 4];
        uvs = new Vector2f[maxSections * 4];
        indices = new int[maxSections * 6];

        material = assetManager.loadMaterial("Materials/Vehicles/SkidMark.j3m");
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Add a section to this skid mark. The alpha component of the vertex color
     * indicates the skid intensity.
     *
     * @param pos the final location for the new section (in world coordinates,
     * not null, unaffected)
     * @param normal the final normal for the new section (a unit vector in
     * world coordinates, not null, unaffected)
     * @param intensity the final intensity for the new section (typically
     * between 0 and 1)
     * @param lastIndex the index of the previous section (&ge;0,
     * &lt;maxSections) or -1 to start a new skid mark
     * @return the index of the new section (&ge;0, &lt;maxSections)
     */
    public int addSection(Vector3f pos, Vector3f normal, float intensity,
            int lastIndex) {
        if (intensity > 1f) {
            intensity = 1f;
        } else if (intensity < 0f) {
            return -1;
        }

        if (lastIndex >= 0) {
            Vector3f lastPosition = sections[lastIndex].position;
            float lengthSquared = pos.distanceSquared(lastPosition);
            if (lengthSquared < minLengthSquared) {
                return lastIndex;
            }
        }

        MarkSection curSection = sections[sectionIndex];

        curSection.position = pos.add(normal.mult(height));
        curSection.normal = normal;
        curSection.intensity = intensity;
        curSection.prevIndex = lastIndex;

        if (lastIndex != -1) {
            MarkSection lastSection = sections[lastIndex];
            Vector3f dir = curSection.position.subtract(lastSection.position);
            Vector3f xDir = dir.cross(normal).normalizeLocal();

            curSection.positionLeft
                    = curSection.position.add(xDir.mult(width / 2f));
            curSection.positionRight
                    = curSection.position.subtract(xDir.mult(width / 2f));
            curSection.tangent = new Vector4f(xDir.x, xDir.y, xDir.z, 1f);

            if (lastSection.prevIndex == -1) {
                lastSection.tangent = curSection.tangent;
                lastSection.positionLeft
                        = curSection.position.add(xDir.mult(width / 2f));
                lastSection.positionRight
                        = curSection.position.subtract(xDir.mult(width / 2f));
            }
        }

        updateSkidMarksMesh();

        int curIndex = sectionIndex;
        // Update circular index
        sectionIndex = ++sectionIndex % maxSections;

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
        if (!needsUpdate) {
            return;
        }
        needsUpdate = false;

        FloatBuffer pb = BufferUtils.createFloatBuffer(positions);
        mesh.setBuffer(VertexBuffer.Type.Position, 3, pb);

        FloatBuffer nb = BufferUtils.createFloatBuffer(normals);
        mesh.setBuffer(VertexBuffer.Type.Normal, 3, nb);

        IntBuffer ib = BufferUtils.createIntBuffer(indices);
        mesh.setBuffer(VertexBuffer.Type.Index, 3, ib);

        FloatBuffer ub = BufferUtils.createFloatBuffer(uvs);
        mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, ub);

        FloatBuffer tb = BufferUtils.createFloatBuffer(tangents);
        mesh.setBuffer(VertexBuffer.Type.Tangent, 4, tb);

        FloatBuffer cb = BufferUtils.createFloatBuffer(colors);
        mesh.setBuffer(VertexBuffer.Type.Color, 4, cb);

        mesh.updateBound();

        if (geometry == null) {
            geometry = new Geometry("SkidMark", mesh);
            geometry.setMaterial(material);
            geometry.setQueueBucket(RenderQueue.Bucket.Transparent);
        }

        geometry.updateModelBound();
    }
    // *************************************************************************
    // private methods

    /**
     * Add 4 mesh vertices (2 triangles) in order to connect the current
     * MarkSection to the previous one.
     */
    private void updateSkidMarksMesh() {
        MarkSection curr = sections[sectionIndex];

        if (curr.prevIndex == -1) {
            // There's nothing to connect to yet.
            return;
        }

        MarkSection last = sections[curr.prevIndex];
        positions[sectionIndex * 4] = last.positionLeft;
        positions[sectionIndex * 4 + 1] = last.positionRight;
        positions[sectionIndex * 4 + 2] = curr.positionLeft;
        positions[sectionIndex * 4 + 3] = curr.positionRight;

        normals[sectionIndex * 4] = last.normal;
        normals[sectionIndex * 4 + 1] = last.normal;
        normals[sectionIndex * 4 + 2] = curr.normal;
        normals[sectionIndex * 4 + 3] = curr.normal;

        tangents[sectionIndex * 4] = last.tangent;
        tangents[sectionIndex * 4 + 1] = last.tangent;
        tangents[sectionIndex * 4 + 2] = curr.tangent;
        tangents[sectionIndex * 4 + 3] = curr.tangent;

        // color of the skid mark
        float r = 43 / 255f;
        float g = 29 / 255f;
        float b = 14 / 255f;

        colors[sectionIndex * 4] = new ColorRGBA(r, g, b, last.intensity);
        colors[sectionIndex * 4 + 1] = new ColorRGBA(r, g, b, last.intensity);
        colors[sectionIndex * 4 + 2] = new ColorRGBA(r, g, b, curr.intensity);
        colors[sectionIndex * 4 + 3] = new ColorRGBA(r, g, b, curr.intensity);

        uvs[sectionIndex * 4] = new Vector2f(0f, 0f);
        uvs[sectionIndex * 4 + 1] = new Vector2f(1f, 0f);
        uvs[sectionIndex * 4 + 2] = new Vector2f(0f, 1f);
        uvs[sectionIndex * 4 + 3] = new Vector2f(1f, 1f);

        indices[sectionIndex * 6] = sectionIndex * 4;
        indices[sectionIndex * 6 + 2] = sectionIndex * 4 + 1;
        indices[sectionIndex * 6 + 1] = sectionIndex * 4 + 2;

        indices[sectionIndex * 6 + 3] = sectionIndex * 4 + 2;
        indices[sectionIndex * 6 + 5] = sectionIndex * 4 + 1;
        indices[sectionIndex * 6 + 4] = sectionIndex * 4 + 3;

        needsUpdate = true;
    }
}
