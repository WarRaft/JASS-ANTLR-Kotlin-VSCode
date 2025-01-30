import * as fs from 'fs'
import * as https from 'https'
import path from 'path'

const download = (url, dest) => {
    const file = fs.createWriteStream(dest)
    https.get(url, function (response) {
        response.pipe(file)
        file.on('finish', () => {
            file.close(() => {
                fs.readFile(dest, {encoding: 'utf8'}, (err, data) => {
                    if (err) return console.log(err)
                    fs.writeFile(
                        dest,
                        data.replace(/\r\n/g, '\n')
                            .replace(/[^\S\r\n]{2,}/g, ' ')
                            .replace(/\n[^\S\r\n]+/g, '\n'), {encoding: 'utf8'}, err => {
                            if (err) return console.log(err)
                        })
                })
            })
        })
    }).on('error', function (err) {
        console.log(err)
    })
}

// https://github.com/UnryzeC/UjAPI/tree/main/uJAPIFiles
download('https://raw.githubusercontent.com/UnryzeC/UjAPI/main/uJAPIFiles/common.j', path.join('..', 'sdk', 'common.j'), true)
download('https://raw.githubusercontent.com/WarRaft/war3mpq/refs/heads/main/extract/Scripts/Blizzard.j', path.join('..', 'sdk', 'blizzard.j'), true)


