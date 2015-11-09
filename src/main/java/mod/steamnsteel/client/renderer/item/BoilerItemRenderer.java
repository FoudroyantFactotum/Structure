package mod.steamnsteel.client.renderer.item;

public class BoilerItemRenderer //implements IItemRenderer
{/*
    private static final ImmutableTriple<Float, Float, Float> ENTITY_OFFSET = ImmutableTriple.of(0.0f, -1.0f, 0.0f);
    private static final ImmutableTriple<Float, Float, Float> EQUIPPED_OFFSET = ImmutableTriple.of(1.0f, -0.50f, 1.2f);
    private static final ImmutableTriple<Float, Float, Float> FIRST_PERSON_OFFSET = ImmutableTriple.of(1.0f, -0.50f, 1.2f);
    private static final ImmutableTriple<Float, Float, Float> INVENTORY_OFFSET = ImmutableTriple.of(1.3f, -1.0f, 1.2f);

    private static final ImmutableTriple<Float, Float, Float> SCALE = ImmutableTriple.of(0.45f, 0.45f, 0.45f);
    private static final ImmutableTriple<Float, Float, Float> INVENTORY_SCALE = ImmutableTriple.of(0.35f, 0.35f, 0.35f);

    private final BoilerModel model;

    public BoilerItemRenderer()
    {
        model = new BoilerModel();
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type)
    {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
    {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data)
    {
        switch (type)
        {
            case ENTITY:
                renderBoiler(ENTITY_OFFSET, SCALE);
                break;
            case EQUIPPED:
                renderBoiler(EQUIPPED_OFFSET, SCALE);
                break;
            case EQUIPPED_FIRST_PERSON:
                renderBoiler(FIRST_PERSON_OFFSET, SCALE);
                break;
            case INVENTORY:
                renderBoiler(INVENTORY_OFFSET, INVENTORY_SCALE);
                break;
            default:
        }
    }

    private void renderBoiler(ImmutableTriple<Float, Float, Float> offset, ImmutableTriple<Float, Float, Float> scale)
    {
        GL11.glPushMatrix();
        GL11.glScalef(scale.left, scale.middle, scale.right);
        GL11.glTranslatef(offset.left, offset.middle, offset.right);

        FMLClientHandler.instance().getClient().renderEngine.bindTexture(BoilerTESR.TEXTURE);

        model.render();

        GL11.glPopMatrix();
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("model", model)
                .toString();
    }*/
}
