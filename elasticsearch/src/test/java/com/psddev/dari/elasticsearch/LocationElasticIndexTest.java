package com.psddev.dari.elasticsearch;

import com.psddev.dari.db.Location;
import com.psddev.dari.db.Region;
import com.psddev.dari.db.Sorter;
import org.junit.Test;

@org.junit.Ignore
public class LocationElasticIndexTest extends AbstractElasticIndexTest<LocationElasticIndexModel, Location> {

    @Override
    protected Class<LocationElasticIndexModel> modelClass() {
        return LocationElasticIndexModel.class;
    }

    @Override
    protected Location value(int index) {
        return new Location(index, index);
    }

    @Test
    public void eqRegion() {
        createCompareTestModels();
        assertCount(4, "one = ?", Region.sphericalCircle(0.0d, 0.0d, 5.5d));
    }

    @Test
    public void eqRegionNotIn() {
        createCompareTestModels();
        assertCount(1, "one != ?", Region.sphericalCircle(0.0d, 0.0d, 5.5d));
    }

    @Test(expected = IllegalArgumentException.class)
    public void eqIllegal() {
        createCompareTestModels();
        query().and("one = true").count();
    }

    @Override
    @Test(expected = IllegalArgumentException.class)
    public void gt() {
        createCompareTestModels();
        query().where("one > ?", value(0)).count();
    }


    @Override
    @Test(expected = IllegalArgumentException.class)
    public void ge() {
        createCompareTestModels();
        query().where("one >= ?", value(0)).count();
    }

    @Override
    @Test(expected = IllegalArgumentException.class)
    public void lt() {
        createCompareTestModels();
        query().where("one < ?", value(0)).count();
    }

    @Override
    @Test(expected = IllegalArgumentException.class)
    public void le() {
        createCompareTestModels();
        query().where("one <= ?", value(0)).count();
    }

    @Override
    @Test(expected = IllegalArgumentException.class)
    public void sortAscendingOne() {
        createSortTestModels();
        query().sortAscending("one").count();
    }

    @Override
    @Test(expected = IllegalArgumentException.class)
    public void sortAscendingReferenceOneOne() {
        super.sortAscendingReferenceOneOne();
    }

    @Override
    @Test(expected = IllegalArgumentException.class)
    public void sortAscendingEmbeddedOneOne() {
        super.sortAscendingEmbeddedOneOne();
    }

    @Override
    @Test(expected = IllegalArgumentException.class)
    public void sortDescendingOne() {
        createSortTestModels();
        query().sortDescending("one").count();
    }

    @Override
    @Test
    public void sortClosestOne() {
        createSortTestModels();
        assertOrder(false, query().sortClosest("one", new Location(0, 0)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void closestIllegalSize() {
        createSortTestModels();
        query().sort(Sorter.CLOSEST_OPERATOR, "one").first();
    }

    @Test(expected = IllegalArgumentException.class)
    public void closestIllegalType() {
        createSortTestModels();
        query().sort(Sorter.CLOSEST_OPERATOR, "one", new Object()).first();
    }

    @Override
    @Test
    public void sortFarthestOne() {
        createSortTestModels();
        assertOrder(true, query().sortFarthest("one", new Location(0, 0)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void farthestIllegalSize() {
        createSortTestModels();
        query().sort(Sorter.FARTHEST_OPERATOR, "one").first();
    }

    @Test(expected = IllegalArgumentException.class)
    public void farthestIllegalType() {
        createSortTestModels();
        query().sort(Sorter.FARTHEST_OPERATOR, "one", new Object()).first();
    }
}
