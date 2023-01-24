package com.akjaw.test.refactor.fruit

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class FruitListViewModelTest {

    companion object {
        private val FRUITS = listOf(Fruit(name = "Apple"), Fruit(name = "Banana"), Fruit(name = "Cherry"))
        private val FRUITS_WITH_FILTER = listOf(Fruit(name = "Apple"), Fruit(name = "Banana"))
    }

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeFruitApi: FakeFruitApi
    private lateinit var systemUnderTest: FruitListViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeFruitApi = FakeFruitApi()
        systemUnderTest = FruitListViewModel(fakeFruitApi)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @Ignore
    fun apiCall() = runTest(testDispatcher) {
        val fruits = KtorFruitApi().getFruits()
        fruits shouldHaveSize 39
    }

    @Test
    fun `Initialize updates the state with fruits fetched from the API sorted by name`() {
        fakeFruitApi.fruits = FRUITS.reversed()

        systemUnderTest.initialize()

        systemUnderTest.fruits.value shouldBe FRUITS
    }

    @Test
    fun `Filtering by name updates the state to include only matching case insensitive fruit names`() {
        fakeFruitApi.fruits = FRUITS
        systemUnderTest.initialize()

        systemUnderTest.filterByName("a")

        systemUnderTest.fruits.value shouldBe FRUITS_WITH_FILTER
    }

    @Test
    fun `Removing the name filter updates the state with the original list`() {
        fakeFruitApi.fruits = FRUITS
        systemUnderTest.initialize()
        systemUnderTest.filterByName("a")

        systemUnderTest.filterByName("")

        systemUnderTest.fruits.value shouldBe FRUITS
    }

    @Test
    fun `Initializing again correctly applies existing name filter`() {
        fakeFruitApi.fruits = FRUITS
        systemUnderTest.initialize()
        systemUnderTest.filterByName("a")

        systemUnderTest.initialize()

        systemUnderTest.fruits.value shouldBe FRUITS_WITH_FILTER
    }

    @Test
    fun `Sorting by CARBOHYDRATES updates the state correctly`() {
        nutritionSortTestCase(
            listOf(
                Fruit(name = "Apple", nutritions = Nutritions(carbohydrates = 10f)),
                Fruit(name = "Banana", nutritions = Nutritions(carbohydrates = 22f)),
                Fruit(name = "Cherry", nutritions = Nutritions(carbohydrates = 11f)),
            ),
            FruitListViewModel.NutritionSortType.CARBOHYDRATES
        )
    }

    @Test
    fun `Sorting by PROTEIN updates the state correctly`() =
        nutritionSortTestCase(
            fruits = listOf(
                Fruit(name = "Apple", nutritions = Nutritions(protein = 10f)),
                Fruit(name = "Banana", nutritions = Nutritions(protein = 22f)),
                Fruit(name = "Cherry", nutritions = Nutritions(protein = 11f)),
            ),
            type = FruitListViewModel.NutritionSortType.PROTEIN
        )

    @Test
    fun `Sorting by FAT updates the state correctly`() =
        nutritionSortTestCase(
            fruits = listOf(
                Fruit(name = "Apple", nutritions = Nutritions(fat = 10f)),
                Fruit(name = "Banana", nutritions = Nutritions(fat = 22f)),
                Fruit(name = "Cherry", nutritions = Nutritions(fat = 11f)),
            ),
            type = FruitListViewModel.NutritionSortType.FAT
        )

    @Test
    fun `Sorting by CALORIES updates the state correctly`() =
        nutritionSortTestCase(
            fruits = listOf(
                Fruit(name = "Apple", nutritions = Nutritions(calories = 10f)),
                Fruit(name = "Banana", nutritions = Nutritions(calories = 22f)),
                Fruit(name = "Cherry", nutritions = Nutritions(calories = 11f)),
            ),
            type = FruitListViewModel.NutritionSortType.CALORIES
        )

    @Test
    fun `Sorting by SUGAR updates the state correctly`() =
        nutritionSortTestCase(
            fruits = listOf(
                Fruit(name = "Apple", nutritions = Nutritions(sugar = 10f)),
                Fruit(name = "Banana", nutritions = Nutritions(sugar = 22f)),
                Fruit(name = "Cherry", nutritions = Nutritions(sugar = 11f)),
            ),
            type = FruitListViewModel.NutritionSortType.SUGAR
        )

    @Test
    fun `Filtering after sorting keeps the correct sorting`() {
        fakeFruitApi.fruits = listOf(
            Fruit(name = "Apple", nutritions = Nutritions(carbohydrates = 3f)),
            Fruit(name = "Banana", nutritions = Nutritions(carbohydrates = 1f)),
            Fruit(name = "Cherry", nutritions = Nutritions(carbohydrates = 2f)),
        )
        systemUnderTest.initialize()
        systemUnderTest.sortByNutrition(FruitListViewModel.NutritionSortType.CARBOHYDRATES)

        systemUnderTest.filterByName("a")

        assertSoftly(systemUnderTest.fruits.value) {
            shouldHaveSize(2)
            get(0).name shouldBe "Banana"
            get(1).name shouldBe "Apple"
        }
    }

    @Test
    fun `Sorting after filtering keeps the correct filter`() {
        fakeFruitApi.fruits = listOf(
            Fruit(name = "Apple", nutritions = Nutritions(carbohydrates = 3f)),
            Fruit(name = "Banana", nutritions = Nutritions(carbohydrates = 1f)),
            Fruit(name = "Cherry", nutritions = Nutritions(carbohydrates = 2f)),
        )
        systemUnderTest.initialize()
        systemUnderTest.filterByName("a")

        systemUnderTest.sortByNutrition(FruitListViewModel.NutritionSortType.CARBOHYDRATES)

        assertSoftly(systemUnderTest.fruits.value) {
            shouldHaveSize(2)
            get(0).name shouldBe "Banana"
            get(1).name shouldBe "Apple"
        }
    }

    @Test
    fun `Removing sorting brings back original sorting`() {
        fakeFruitApi.fruits = listOf(
            Fruit(name = "Apple", nutritions = Nutritions(carbohydrates = 3f)),
            Fruit(name = "Banana", nutritions = Nutritions(carbohydrates = 1f)),
            Fruit(name = "Cherry", nutritions = Nutritions(carbohydrates = 2f)),
        )
        systemUnderTest.initialize()
        systemUnderTest.sortByNutrition(FruitListViewModel.NutritionSortType.CARBOHYDRATES)

        systemUnderTest.sortByNutrition(FruitListViewModel.NutritionSortType.NO_SORTING)
        systemUnderTest.sortByNutrition(FruitListViewModel.NutritionSortType.NO_SORTING)
        systemUnderTest.sortByNutrition(FruitListViewModel.NutritionSortType.NO_SORTING)

        assertSoftly(systemUnderTest.fruits.value) {
            shouldHaveSize(3)
            get(0).name shouldBe "Apple"
            get(1).name shouldBe "Banana"
            get(2).name shouldBe "Cherry"
        }
    }

    @Test
    fun `Initially the favorite fruits are empty`() {
        systemUnderTest.favoriteFruitIds.value shouldBe emptyList()
    }

    @Test
    fun `Adding a favorite fruit updates the favorite list`() {
        systemUnderTest.addToFavorite(1)

        systemUnderTest.favoriteFruitIds.value shouldBe listOf(1)
    }

    @Test
    fun `Adding the same favorite fruit multiple times does not duplicate the id`() {
        systemUnderTest.addToFavorite(1)
        systemUnderTest.addToFavorite(1)
        systemUnderTest.addToFavorite(1)
        systemUnderTest.addToFavorite(1)

        systemUnderTest.favoriteFruitIds.value shouldBe listOf(1)
    }

    @Test
    fun `Favorite fruits are placed at the top when there is no sorting`() {
        fakeFruitApi.fruits = listOf(
            Fruit(name = "Apple", id = 3),
            Fruit(name = "Banana", id = 2),
            Fruit(name = "Cherry", id = 1),
        )
        systemUnderTest.initialize()

        systemUnderTest.addToFavorite(1)

        assertSoftly(systemUnderTest.fruits.value) {
            shouldHaveSize(3)
            get(0).name shouldBe "Cherry"
            get(1).name shouldBe "Apple"
            get(2).name shouldBe "Banana"
        }
    }

    @Test
    fun `Favorite fruits dont change the activated sorting`() {
        fakeFruitApi.fruits = listOf(
            Fruit(name = "Apple", id = 3),
            Fruit(name = "Banana", id = 2),
            Fruit(name = "Cherry", id = 1),
        )
        systemUnderTest.initialize()
        systemUnderTest.sortByNutrition(FruitListViewModel.NutritionSortType.CARBOHYDRATES)

        systemUnderTest.addToFavorite(1)

        assertSoftly(systemUnderTest.fruits.value) {
            shouldHaveSize(3)
            get(0).name shouldBe "Apple"
            get(1).name shouldBe "Banana"
            get(2).name shouldBe "Cherry"
        }
    }

    private fun nutritionSortTestCase(fruits: List<Fruit>, type: FruitListViewModel.NutritionSortType) {
        fakeFruitApi.fruits = fruits
        systemUnderTest.initialize()

        systemUnderTest.sortByNutrition(nutrition = type)

        assertSoftly(systemUnderTest.fruits.value) {
            systemUnderTest.fruits.value.shouldHaveSize(3)
            systemUnderTest.fruits.value.get(0).name shouldBe "Apple"
            systemUnderTest.fruits.value.get(1).name shouldBe "Cherry"
            systemUnderTest.fruits.value.get(2).name shouldBe "Banana"
        }
    }
}

class FakeFruitApi : FruitApi {

    var fruits: List<Fruit> = emptyList()

    override suspend fun getFruits(): List<Fruit> = fruits
}