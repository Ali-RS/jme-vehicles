package com.jayfella.jme.vehicle.skid;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.mesh.IndexBuffer;
import com.jme3.util.BufferUtils;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import jme3utilities.math.MyBuffer;

/**
 * A single continuous skid mark, composed of straight sections and rendered by
 * a single Geometry.
 */
public class SkidMarkManager {
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
    /**
     * number of axes in the coordinate system
     */
    final private static int numAxes = 3;
    /**
     * number of vertices per triangle
     */
    final public static int vpt = 3;
    // *************************************************************************
    // fields

    /**
     * width of this skid mark (in meters), should match the width of the tire
     */
    final private float width;
    /**
     * Geometry to visualize this skid mark
     */
    final private Geometry geometry;
    /**
     * capacity of the Mesh in terms of sections
     */
    private int meshSize;
    /**
     * list of sections
     */
    final private List<SkidMarkSection> sections;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a continuous skid mark with the specified width.
     *
     * @param assetManager for loading assets (not null)
     * @param tireWidth the desired width of this skid mark (in meters, &gt;0)
     */
    public SkidMarkManager(AssetManager assetManager, float tireWidth) {
        width = tireWidth;
        sections = new ArrayList<>(33);
        geometry = createGeometry(assetManager, 32);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Add a section to this skid mark. The alpha component of the vertex color
     * indicates the skid intensity.
     *
     * @param pavementLocation the final pavement location for the new section
     * (in world coordinates, not null, unaffected)
     * @param normal the final normal for the new section (a unit vector in
     * world coordinates, not null, unaffected)
     * @param opacity the final intensity for the new section (typically between
     * 0 and 1)
     * @param prevIndex the index of the previous final section (&ge;0,
     * &lt;maxSections) or -1 to start a new skid mark
     * @return the index of the final section (&ge;0, &lt;maxSections) or -1 if
     * the skid has ended
     */
    public int addSection(Vector3f pavementLocation, Vector3f normal,
            float opacity, int prevIndex) {
        if (opacity < 0f) {
            return -1;
        }
        if (opacity > 1f) {
            opacity = 1f;
        }

        SkidMarkSection previous;
        if (prevIndex == -1) {
            sections.clear();
            previous = null;
        } else {
            previous = sections.get(prevIndex);
            float lengthSquared = previous.distanceSquared(pavementLocation);
            if (lengthSquared < minLengthSquared) {
                return prevIndex;
            }
        }

        SkidMarkSection section = new SkidMarkSection(pavementLocation, normal,
                height, opacity, previous, width / 2f);
        sections.add(section);
        int numSections = sections.size();
        int sectionIndex = numSections - 1;
        if (numSections - 1 > meshSize) {
            /*
             * Recreate the Mesh from sections, making each buffer 4x larger.
             */
            Mesh mesh = createMesh(4 * meshSize);
            geometry.setMesh(mesh);
            for (int i = 0; i < sectionIndex; ++i) {
                SkidMarkSection s = sections.get(i);
                s.appendToMesh(mesh, i);
            }
        }
        /**
         * Add 4 mesh vertices (2 triangles) in order to connect the new section
         * to its predecessor.
         */
        Mesh mesh = geometry.getMesh();
        section.appendToMesh(mesh, sectionIndex);

        return sectionIndex;
    }

    /**
     * Access the Geometry used to render the marks.
     *
     * @return the pre-existing instance (not null)
     */
    public Geometry getGeometry() {
        return geometry;
    }
    // *************************************************************************
    // private methods

    private Geometry createGeometry(AssetManager assetManager, int numSections) {
        Mesh mesh = createMesh(numSections);
        Geometry result = new Geometry("Skid Mark", mesh);
        /*
         * Disable culling so we can skip updating the mesh bounds.
         */
        result.setCullHint(Spatial.CullHint.Never);

        Material material
                = assetManager.loadMaterial("Materials/Vehicles/SkidMark.j3m");
        result.setMaterial(material);
        result.setQueueBucket(RenderQueue.Bucket.Transparent);

        return result;
    }

    private Mesh createMesh(int numSections) {
        int numTriangles = numSections * 2;
        int numVertices = numSections * 4;
        int numIndices = numTriangles * vpt;
        IndexBuffer indexBuffer
                = IndexBuffer.createIndexBuffer(numVertices, numIndices);
        VertexBuffer.Format ibFormat = MyBuffer.getFormat(indexBuffer);
        Buffer ibData = indexBuffer.getBuffer();

        FloatBuffer colorBuffer
                = BufferUtils.createFloatBuffer(numVertices * 4);
        FloatBuffer normalBuffer
                = BufferUtils.createFloatBuffer(numVertices * numAxes);
        FloatBuffer positionBuffer
                = BufferUtils.createFloatBuffer(numVertices * numAxes);
        FloatBuffer tangentBuffer
                = BufferUtils.createFloatBuffer(numVertices * 4);
        FloatBuffer uvBuffer = BufferUtils.createFloatBuffer(numVertices * 2);

        Mesh result = new Mesh();
        result.setBuffer(VertexBuffer.Type.Index, vpt, ibFormat, ibData);
        result.setBuffer(VertexBuffer.Type.Color, 4, colorBuffer);
        result.setBuffer(VertexBuffer.Type.Normal, numAxes, normalBuffer);
        result.setBuffer(VertexBuffer.Type.Position, numAxes, positionBuffer);
        result.setBuffer(VertexBuffer.Type.Tangent, 4, tangentBuffer);
        result.setBuffer(VertexBuffer.Type.TexCoord, 2, uvBuffer);
        result.setDynamic();

        meshSize = numSections;

        return result;
    }
}
