package forpdateam.ru.forpda.model.repository.devdb

import forpdateam.ru.forpda.model.data.remote.api.devdb.DevDb
import forpdateam.ru.forpda.entity.remote.devdb.Brand
import forpdateam.ru.forpda.entity.remote.devdb.Brands
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.entity.remote.devdb.DeviceSearch
import forpdateam.ru.forpda.model.SchedulersProvider
import io.reactivex.Observable

/**
 * Created by radiationx on 01.01.18.
 */

class DevDbRepository(
        private val schedulers: SchedulersProvider,
        private val devDbApi: DevDb
) {

    fun getBrands(catId: String): Observable<Brands> = Observable
            .fromCallable { devDbApi.getBrands(catId) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getBrand(catId: String, brandId: String): Observable<Brand> = Observable
            .fromCallable { devDbApi.getBrand(catId, brandId) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getDevice(devId: String): Observable<Device> = Observable
            .fromCallable { devDbApi.getDevice(devId) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun search(query: String): Observable<DeviceSearch> = Observable
            .fromCallable { devDbApi.search(query) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

}
