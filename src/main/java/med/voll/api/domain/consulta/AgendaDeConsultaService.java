package med.voll.api.domain.consulta;

import med.voll.api.domain.consulta.desafio.ValidadorCancelamientoDeConsulta;
import med.voll.api.domain.consulta.validaciones.HorarioDeAnticipacion;
import med.voll.api.domain.consulta.validaciones.ValidadorDeConsultas;
import med.voll.api.domain.medico.Medico;
import med.voll.api.domain.medico.MedicoRepository;
import med.voll.api.domain.paciente.PacienteRepository;
import med.voll.api.infra.errores.ValidacionDeIntegridad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgendaDeConsultaService {
    @Autowired
    private ConsultaRepository consultaRepository;
    @Autowired
    private MedicoRepository medicoRepository;
    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    List<ValidadorDeConsultas> validadores;
    @Autowired
    List<ValidadorCancelamientoDeConsulta> validadoresCancelamiento;

    public DatosDetalleConsulta agendar(DatosAgendarConsulta datos){
        if(!pacienteRepository.findById(datos.idPaciente()).isPresent()){
            throw new ValidacionDeIntegridad("El paciente con id " + datos.idPaciente() + " no fue encontrado.");

        }

        if(datos.idMedico()!=null && !medicoRepository.existsById(datos.idMedico())){
            throw new ValidacionDeIntegridad("El médico con id " + datos.idMedico() + " no fue encontrado");
        }

        //Validaciones provenientes de la lista
        validadores.forEach(v -> v.validar(datos));

        var paciente = pacienteRepository.findById(datos.idPaciente()).get();
        var medico = seleccionarMedico(datos);
        if(medico == null){
            throw new ValidacionDeIntegridad("No existen médicos disponibles para este horario y especialidad.");

        }
        var consulta = new Consulta(medico, paciente, datos.fecha());
        consultaRepository.save(consulta);
        return new DatosDetalleConsulta(consulta);
    }
    public void cancelar(DatosCancelamientoConsulta datos){
        if(!consultaRepository.existsById(datos.idConsulta())){
            throw new ValidacionDeIntegridad("No existe una consulta con ese id.");
        }
        validadoresCancelamiento.forEach(v -> v.validar(datos));
        var consulta = consultaRepository.getReferenceById(datos.idConsulta());
        consulta.cancelar(datos.motivo());
    }

    private Medico seleccionarMedico(DatosAgendarConsulta datos) {
        if(datos.idMedico()!=null){
            return medicoRepository.getReferenceById(datos.idMedico());
        }
        if(datos.especialidad()==null){
            throw new ValidacionDeIntegridad("Debe seleccionar una especialidad para el médico.");
        }
        return medicoRepository.seleccionarMedicoConEspecialidadEnFecha(datos.especialidad(), datos.fecha());
    }
}
